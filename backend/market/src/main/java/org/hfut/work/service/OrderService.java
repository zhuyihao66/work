package org.hfut.work.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hfut.work.entity.Address;
import org.hfut.work.entity.Item;
import org.hfut.work.entity.Order;
import org.hfut.work.entity.OrderItem;
import org.hfut.work.mapper.ItemMapper;
import org.hfut.work.mapper.ItemImageMapper;
import org.hfut.work.mapper.OrderItemMapper;
import org.hfut.work.mapper.OrderMapper;
import org.hfut.work.mapper.AddressMapper;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class OrderService {

    @Resource
    private ItemMapper itemMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderItemMapper orderItemMapper;
    @Resource
    private ItemImageMapper itemImageMapper;
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private NotificationService notificationService;

    private static final String KEY_ITEM_DETAIL = "item:detail:";
    private static final String KEY_ITEM_LIST_KEYS = "item:list:keys";
    @Resource
    private AddressMapper addressMapper;
    @Resource
    private org.hfut.work.mapper.UserMapper userMapper;

    @Transactional
    public Order createFixedPriceOrder(Long buyerId, Long itemId, int quantity, String note) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        if (!"listed".equalsIgnoreCase(item.getStatus())) {
            throw new IllegalStateException("商品不可购买");
        }
        if (item.getQuantity() == null || item.getQuantity() < quantity) {
            throw new IllegalStateException("库存不足");
        }

        BigDecimal total = item.getPrice().multiply(new BigDecimal(quantity));
        String orderNo = generateOrderNo();

        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setBuyerId(buyerId);
        order.setSellerId(item.getSellerId());
        order.setStatus("pending");
        order.setTotalAmount(total);
        order.setCurrency(item.getCurrency());
        order.setExpiresAt(LocalDateTime.now().plusMinutes(1));
        // address snapshot in note JSON if provided as addressId=123
        String finalNote = note;
        try {
            if (note != null && note.startsWith("addressId=")) {
                Long addrId = Long.valueOf(note.substring("addressId=".length()));
                org.hfut.work.entity.Address a = addressMapper.selectById(addrId);
                if (a != null && a.getUserId().equals(buyerId)) {
                    Map<String, Object> snap = getStringObjectMap(a);
                    finalNote = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(snap);
                }
            }
        } catch (Exception ignored) {}
        order.setNote(finalNote);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        OrderItem oi = new OrderItem();
        oi.setOrderId(order.getId());
        oi.setItemId(item.getId());
        oi.setTitleSnapshot(item.getTitle());
        oi.setPriceSnapshot(item.getPrice());
        oi.setQuantity(quantity);
        orderItemMapper.insert(oi);

        int remain = item.getQuantity() - quantity;
        item.setQuantity(remain);
        if (remain <= 0) {
            item.setStatus("sold");
        }
        item.setUpdatedAt(LocalDateTime.now());
        itemMapper.updateById(item);

        // evict item caches
        try {
            redis.delete(KEY_ITEM_DETAIL + item.getId());
            java.util.Set<String> keys = redis.opsForSet().members(KEY_ITEM_LIST_KEYS);
            if (keys != null && !keys.isEmpty()) { redis.delete(keys); redis.delete(KEY_ITEM_LIST_KEYS); }
        } catch (Exception ignored) {}

        notifyBuyerOrderCreated(order, item);
        notifySellerNewOrder(order, item, buyerId);
        return order;
    }

    private static Map<String, Object> getStringObjectMap(Address a) {
        Map<String,Object> snap = new HashMap<>();
        Map<String,Object> addr = new HashMap<>();
        addr.put("receiverName", a.getReceiverName());
        addr.put("phone", a.getPhone());
        addr.put("country", a.getCountry());
        addr.put("province", a.getProvince());
        addr.put("city", a.getCity());
        addr.put("district", a.getDistrict());
        addr.put("addressLine", a.getAddressLine());
        addr.put("postalCode", a.getPostalCode());
        snap.put("addressSnapshot", addr);
        return snap;
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + (int)(Math.random()*1000);
    }

    public List<Map<String, Object>> listBuyerOrders(Long buyerId, int limit, int offset, String status, String q) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .eq(Order::getBuyerId, buyerId);
        if (status != null && !status.isEmpty()) {
            qw.eq(Order::getStatus, status);
        }
        if (q != null && !q.isEmpty()) {
            // (buyerId = me) AND (orderNo like q OR id IN matchedIds)
            List<OrderItem> likeItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().like(OrderItem::getTitleSnapshot, q));
            java.util.Set<Long> orderIds = new java.util.HashSet<>();
            if (likeItems != null) {
                for (OrderItem it : likeItems) {
                    orderIds.add(it.getOrderId());
                }
            }
            qw.and(w -> w.like(Order::getOrderNo, q)
                    .or(orderIds != null && !orderIds.isEmpty(), x -> x.in(Order::getId, orderIds)));
        }
        qw.orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(qw.last("limit " + limit + " offset " + offset));
        Map<Long, List<Map<String,Object>>> itemsByOrder = new HashMap<>();
        for (Order o : orders) {
            List<OrderItem> oi = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
            List<Map<String,Object>> enriched = new java.util.ArrayList<>();
            for (OrderItem it : oi) {
                Map<String,Object> m = new HashMap<>();
                m.put("id", it.getId());
                m.put("itemId", it.getItemId());
                m.put("title", it.getTitleSnapshot());
                m.put("price", it.getPriceSnapshot());
                m.put("quantity", it.getQuantity());
                Item item = itemMapper.selectById(it.getItemId());
                if (item != null) {
                    m.put("description", item.getDescription());
                    org.hfut.work.entity.ItemImage img = itemImageMapper.selectOne(new LambdaQueryWrapper<org.hfut.work.entity.ItemImage>().eq(org.hfut.work.entity.ItemImage::getItemId, it.getItemId()).orderByAsc(org.hfut.work.entity.ItemImage::getSortOrder).last("limit 1"));
                    if (img != null) {
                        m.put("imageUrl", img.getImageUrl());
                    }
                }
                enriched.add(m);
            }
            itemsByOrder.put(o.getId(), enriched);
        }
        return orders.stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo());
            m.put("status", o.getStatus());
            m.put("totalAmount", o.getTotalAmount());
            m.put("currency", o.getCurrency());
            m.put("createdAt", o.getCreatedAt());
            m.put("expiresAt", o.getExpiresAt());
            m.put("remainingSeconds", remainingSeconds(o));
            // 添加卖家信息
            org.hfut.work.entity.User seller = userMapper.selectById(o.getSellerId());
            if (seller != null) {
                Map<String, Object> sellerMap = new HashMap<>();
                sellerMap.put("id", seller.getId());
                sellerMap.put("displayName", seller.getDisplayName());
                sellerMap.put("avatarUrl", seller.getAvatarUrl());
                m.put("seller", sellerMap);
            }
            List<Map<String,Object>> oi = itemsByOrder.get(o.getId());
            if (oi != null && !oi.isEmpty()) {
                m.put("title", oi.get(0).get("title"));
                m.put("quantity", oi.get(0).get("quantity"));
                m.put("items", oi);
            }
            return m;
        }).toList();
    }

    public Map<String, Object> getBuyerOrderDetail(Long buyerId, Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(buyerId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        return buildOrderDetailMap(o);
    }

    public Map<String, Object> getSellerOrderDetail(Long sellerId, Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null || !o.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        return buildOrderDetailMap(o);
    }

    private Map<String, Object> buildOrderDetailMap(Order o) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", o.getId());
        m.put("orderNo", o.getOrderNo());
        m.put("status", o.getStatus());
        m.put("totalAmount", o.getTotalAmount());
        m.put("currency", o.getCurrency());
        m.put("createdAt", o.getCreatedAt());
        m.put("expiresAt", o.getExpiresAt());
        m.put("remainingSeconds", remainingSeconds(o));
        // 添加买家信息
        org.hfut.work.entity.User buyer = userMapper.selectById(o.getBuyerId());
        if (buyer != null) {
            Map<String, Object> buyerMap = new HashMap<>();
            buyerMap.put("id", buyer.getId());
            buyerMap.put("displayName", buyer.getDisplayName());
            buyerMap.put("avatarUrl", buyer.getAvatarUrl());
            m.put("buyer", buyerMap);
        }
        // 添加卖家信息
        org.hfut.work.entity.User seller = userMapper.selectById(o.getSellerId());
        if (seller != null) {
            Map<String, Object> sellerMap = new HashMap<>();
            sellerMap.put("id", seller.getId());
            sellerMap.put("displayName", seller.getDisplayName());
            sellerMap.put("avatarUrl", seller.getAvatarUrl());
            m.put("seller", sellerMap);
        }
        List<OrderItem> oi = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
        List<Map<String,Object>> enriched = new java.util.ArrayList<>();
        for (OrderItem it : oi) {
            Map<String,Object> im = new HashMap<>();
            im.put("id", it.getId());
            im.put("itemId", it.getItemId());
            im.put("title", it.getTitleSnapshot());
            im.put("price", it.getPriceSnapshot());
            im.put("quantity", it.getQuantity());
            Item item = itemMapper.selectById(it.getItemId());
            if (item != null) {
                im.put("description", item.getDescription());
                org.hfut.work.entity.ItemImage img = itemImageMapper.selectOne(new LambdaQueryWrapper<org.hfut.work.entity.ItemImage>().eq(org.hfut.work.entity.ItemImage::getItemId, it.getItemId()).orderByAsc(org.hfut.work.entity.ItemImage::getSortOrder).last("limit 1"));
                if (img != null) {
                    im.put("imageUrl", img.getImageUrl());
                }
            }
            enriched.add(im);
        }
        m.put("items", enriched);
        return m;
    }

    public List<Map<String, Object>> listSellerOrders(Long sellerId, int limit, int offset, String status, String q) {
        LambdaQueryWrapper<Order> qw = new LambdaQueryWrapper<Order>()
                .eq(Order::getSellerId, sellerId);
        if (status != null && !status.isEmpty()) {
            qw.eq(Order::getStatus, status);
        }
        if (q != null && !q.isEmpty()) {
            List<OrderItem> likeItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().like(OrderItem::getTitleSnapshot, q));
            java.util.Set<Long> orderIds = new java.util.HashSet<>();
            if (likeItems != null) {
                for (OrderItem it : likeItems) {
                    orderIds.add(it.getOrderId());
                }
            }
            qw.and(w -> w.like(Order::getOrderNo, q)
                    .or(orderIds != null && !orderIds.isEmpty(), x -> x.in(Order::getId, orderIds)));
        }
        qw.orderByDesc(Order::getCreatedAt);
        List<Order> orders = orderMapper.selectList(qw.last("limit " + limit + " offset " + offset));
        Map<Long, List<Map<String,Object>>> itemsByOrder = new HashMap<>();
        for (Order o : orders) {
            List<OrderItem> oi = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, o.getId()));
            List<Map<String,Object>> enriched = new java.util.ArrayList<>();
            for (OrderItem it : oi) {
                Map<String,Object> m = new HashMap<>();
                m.put("id", it.getId());
                m.put("itemId", it.getItemId());
                m.put("title", it.getTitleSnapshot());
                m.put("price", it.getPriceSnapshot());
                m.put("quantity", it.getQuantity());
                Item item = itemMapper.selectById(it.getItemId());
                if (item != null) {
                    m.put("description", item.getDescription());
                    org.hfut.work.entity.ItemImage img = itemImageMapper.selectOne(new LambdaQueryWrapper<org.hfut.work.entity.ItemImage>().eq(org.hfut.work.entity.ItemImage::getItemId, it.getItemId()).orderByAsc(org.hfut.work.entity.ItemImage::getSortOrder).last("limit 1"));
                    if (img != null) {
                        m.put("imageUrl", img.getImageUrl());
                    }
                }
                enriched.add(m);
            }
            itemsByOrder.put(o.getId(), enriched);
        }
        return orders.stream().map(o -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", o.getId());
            m.put("orderNo", o.getOrderNo());
            m.put("status", o.getStatus());
            m.put("totalAmount", o.getTotalAmount());
            m.put("currency", o.getCurrency());
            m.put("createdAt", o.getCreatedAt());
            m.put("expiresAt", o.getExpiresAt());
            // 添加买家信息
            org.hfut.work.entity.User buyer = userMapper.selectById(o.getBuyerId());
            if (buyer != null) {
                Map<String, Object> buyerMap = new HashMap<>();
                buyerMap.put("id", buyer.getId());
                buyerMap.put("displayName", buyer.getDisplayName());
                buyerMap.put("avatarUrl", buyer.getAvatarUrl());
                m.put("buyer", buyerMap);
            }
            List<Map<String,Object>> oi = itemsByOrder.get(o.getId());
            if (oi != null && !oi.isEmpty()) {
                m.put("title", oi.get(0).get("title"));
                m.put("quantity", oi.get(0).get("quantity"));
                m.put("items", oi);
            }
            return m;
        }).toList();
    }

    public void cancelByBuyer(Long buyerId, Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(buyerId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"pending".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalStateException("当前状态不可取消");
        }
        cancelAndRestore(o);
    }

    public void markPaid(Long buyerId, Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(buyerId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"pending".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalStateException("当前状态不可支付");
        }
        o.setStatus("paid");
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
        notificationService.notifyUser(o.getSellerId(), "seller_order_paid", Map.of(
                "orderId", o.getId(),
                "orderNo", o.getOrderNo(),
                "totalAmount", o.getTotalAmount()
        ));
    }

    public void shipBySeller(Long sellerId, Long orderId, String provider, String trackingNo, org.hfut.work.mapper.ShipmentMapper shipmentMapper) {
        Order o = orderMapper.selectById(orderId);
        if (o == null || !o.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("订单不存在");
        }

        if (!"paid".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalStateException("未付款订单不可发货");
        }
        org.hfut.work.entity.Shipment s = new org.hfut.work.entity.Shipment();
        s.setOrderId(orderId);
        s.setShippingProvider(provider);
        s.setTrackingNo(trackingNo);
        s.setStatus("shipped");
        s.setShippedAt(LocalDateTime.now());
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        shipmentMapper.insert(s);
        o.setStatus("shipped");
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
    }

    public void confirmDelivered(Long buyerId, Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null || !o.getBuyerId().equals(buyerId)) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"shipped".equalsIgnoreCase(o.getStatus())) {
            throw new IllegalStateException("当前状态不可确认收货");
        }
        o.setStatus("completed");
        o.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(o);
    }

    @Transactional
    public void cancelExpiredOrder(Order order) {
        if (order == null || !"pending".equalsIgnoreCase(order.getStatus())) {
            return;
        }
        cancelAndRestore(order);
        notificationService.notifyUser(order.getBuyerId(), "order_timeout", Map.of(
                "orderId", order.getId(),
                "orderNo", order.getOrderNo()
        ));
    }

    private void cancelAndRestore(Order order) {
        order.setStatus("cancelled");
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        List<OrderItem> oi = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem it : oi) {
            Item item = itemMapper.selectById(it.getItemId());
            if (item != null) {
                item.setQuantity((item.getQuantity() == null ? 0 : item.getQuantity()) + it.getQuantity());
                if ("sold".equalsIgnoreCase(item.getStatus())) {
                    item.setStatus("listed");
                }
                item.setUpdatedAt(LocalDateTime.now());
                itemMapper.updateById(item);
            }
        }
    }

    private void notifyBuyerOrderCreated(Order order, Item item) {
        notificationService.notifyUser(order.getBuyerId(), "order_created", Map.of(
                "orderId", order.getId(),
                "orderNo", order.getOrderNo(),
                "totalAmount", order.getTotalAmount(),
                "currency", order.getCurrency(),
                "expiresAt", order.getExpiresAt(),
                "remainingSeconds", remainingSeconds(order),
                "itemTitle", item.getTitle()
        ));
    }

    private void notifySellerNewOrder(Order order, Item item, Long buyerId) {
        notificationService.notifyUser(order.getSellerId(), "seller_new_order", Map.of(
                "orderId", order.getId(),
                "orderNo", order.getOrderNo(),
                "itemTitle", item.getTitle(),
                "buyerId", buyerId,
                "status", order.getStatus()
        ));
    }

    private long remainingSeconds(Order order) {
        if (order.getExpiresAt() == null || !"pending".equalsIgnoreCase(order.getStatus())) {
            return 0;
        }
        long remaining = ChronoUnit.SECONDS.between(LocalDateTime.now(), order.getExpiresAt());
        return Math.max(0, remaining);
    }
}


