package org.hfut.work.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hfut.work.entity.Item;
import org.hfut.work.entity.ItemImage;
import org.hfut.work.entity.User;
import org.hfut.work.entity.Review;
import org.hfut.work.dto.ItemDtos;
import org.hfut.work.mapper.ItemImageMapper;
import org.hfut.work.mapper.ItemMapper;
import org.hfut.work.mapper.UserMapper;
import org.hfut.work.mapper.ReviewMapper;
import org.hfut.work.mapper.CategoryMapper;
import org.hfut.work.entity.Category;
import org.hfut.work.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Resource
    private ItemMapper itemMapper;
    @Resource
    private ItemImageMapper itemImageMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ReviewMapper reviewMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private ReviewService reviewService;
    @Resource
    private StringRedisTemplate redis;
    @Resource
    private org.hfut.work.mapper.OrderItemMapper orderItemMapper;
    @Resource
    private org.hfut.work.mapper.OrderMapper orderMapper;

    private static final String KEY_ITEM_DETAIL = "item:detail:"; // + id
    private static final String KEY_ITEM_LIST_KEYS = "item:list:keys"; // set of keys

    public Map<String, Object> getDetail(Long id) {
        // 注意：不使用缓存，因为需要根据当前登录用户动态判断是否是本人上架的商品
        // 如果使用缓存，可能导致 seller.id 判断不准确
        // try {
        //     String cached = redis.opsForValue().get(KEY_ITEM_DETAIL + id);
        //     if (cached != null) {
        //         return new com.fasterxml.jackson.databind.ObjectMapper().readValue(cached, java.util.Map.class);
        //     }
        // } catch (Exception ignored) {}

        Item item = itemMapper.selectById(id);
        if (item == null) throw new IllegalArgumentException("商品不存在");
        List<ItemImage> images = itemImageMapper.selectList(new LambdaQueryWrapper<ItemImage>().eq(ItemImage::getItemId, id).orderByAsc(ItemImage::getSortOrder));
        User seller = userMapper.selectById(item.getSellerId());
        Map<String, Object> m = new HashMap<>();
        m.put("id", item.getId());
        m.put("title", item.getTitle());
        m.put("price", item.getPrice());
        m.put("currency", item.getCurrency());
        m.put("description", item.getDescription());
        m.put("condition", item.getItemCondition());
        m.put("location", item.getLocationText());
        m.put("status", item.getStatus());
        m.put("quantity", item.getQuantity());
        m.put("images", images.stream().map(ItemImage::getImageUrl).collect(Collectors.toList()));
        Map<String, Object> sellerMap = new HashMap<>();
        if (seller != null) {
            sellerMap.put("id", seller.getId());
            sellerMap.put("displayName", seller.getDisplayName());
            sellerMap.put("avatarUrl", seller.getAvatarUrl());
        }
        m.put("seller", sellerMap);
        // 获取该商品的所有评论（通过订单关联）
        List<Map<String,Object>> reviewList = reviewService.getReviewsByItemId(id);
        m.put("reviews", reviewList);
        // 不缓存详情，因为需要根据登录用户动态判断是否是本人上架的商品
        // try {
        //     String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(m);
        //     redis.opsForValue().set(KEY_ITEM_DETAIL + id, json, java.time.Duration.ofSeconds(300));
        // } catch (Exception ignored) {}
        return m;
    }

    public List<Map<String, Object>> listHome(int limit, int offset, String q, Long categoryId) {
        LambdaQueryWrapper<Item> qw = new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, "listed");
        if (q != null && !q.isEmpty()) {
            qw.like(Item::getTitle, q);
        }
        if (categoryId != null) {
            qw.eq(Item::getCategoryId, categoryId);
        }
        // 注意：不使用缓存，因为需要根据当前登录用户动态显示 seller 信息
        // 如果使用缓存，会导致 seller 信息不一致，影响"我的商品"标识显示
        // String listKey = String.format("item:list:l%do%dq%sC%s", limit, offset, q == null ? "" : q, categoryId == null ? "" : categoryId.toString());
        // try {
        //     String cached = redis.opsForValue().get(listKey);
        //     if (cached != null) {
        //         return new com.fasterxml.jackson.databind.ObjectMapper().readValue(cached, java.util.List.class);
        //     }
        // } catch (Exception ignored) {}

        List<Item> items = itemMapper.selectList(qw.orderByDesc(Item::getCreatedAt)
                .last("limit " + limit + " offset " + offset));
        java.util.List<java.util.Map<String,Object>> list = items.stream().map(it -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", it.getId());
            m.put("title", it.getTitle());
            m.put("description", it.getDescription());
            m.put("price", it.getPrice());
            m.put("currency", it.getCurrency());
            // 添加卖家信息（仅包含ID，用于前端判断是否是本人上架）
            Map<String, Object> sellerMap = new HashMap<>();
            sellerMap.put("id", it.getSellerId());
            m.put("seller", sellerMap);
            // cover image: first by sort_order
            List<ItemImage> imgs = itemImageMapper.selectList(new LambdaQueryWrapper<ItemImage>()
                    .eq(ItemImage::getItemId, it.getId())
                    .orderByAsc(ItemImage::getSortOrder)
                    .last("limit 1"));
            m.put("image", imgs.isEmpty() ? null : imgs.get(0).getImageUrl());
            return m;
        }).collect(Collectors.toList());
        // 不缓存列表，因为需要根据登录用户动态显示 seller 信息
        // try {
        //     String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(list);
        //     redis.opsForValue().set(listKey, json, java.time.Duration.ofSeconds(120));
        //     redis.opsForSet().add(KEY_ITEM_LIST_KEYS, listKey);
        // } catch (Exception ignored) {}
        return list;
    }

    private void evictItemDetail(Long id) { redis.delete(KEY_ITEM_DETAIL + id); }
    private void evictAllLists() {
        try {
            java.util.Set<String> keys = redis.opsForSet().members(KEY_ITEM_LIST_KEYS);
            if (keys != null && !keys.isEmpty()) {
                redis.delete(keys);
                redis.delete(KEY_ITEM_LIST_KEYS);
            }
        } catch (Exception ignored) {}
    }

    public Map<String, Long> nav(Long currentId) {
        Item cur = itemMapper.selectById(currentId);
        if (cur == null) throw new IllegalArgumentException("商品不存在");
        // Order: created_at desc; prev = newer, next = older
        Item newer = itemMapper.selectOne(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, "listed")
                .gt(Item::getCreatedAt, cur.getCreatedAt())
                .orderByAsc(Item::getCreatedAt)
                .last("limit 1"));
        Item older = itemMapper.selectOne(new LambdaQueryWrapper<Item>()
                .eq(Item::getStatus, "listed")
                .lt(Item::getCreatedAt, cur.getCreatedAt())
                .orderByDesc(Item::getCreatedAt)
                .last("limit 1"));
        Map<String, Long> m = new HashMap<>();
        m.put("prevId", newer == null ? null : newer.getId());
        m.put("nextId", older == null ? null : older.getId());
        return m;
    }

    public Long createItem(Long sellerId, ItemDtos.CreateRequest req) {
        Item it = new Item();
        it.setSellerId(sellerId);
        it.setTitle(req.getTitle());
        it.setDescription(req.getDescription());
        it.setPrice(req.getPrice());
        it.setCurrency(req.getCurrency());
        it.setItemCondition(req.getCondition());
        it.setQuantity(req.getQuantity() == null ? 1 : req.getQuantity());
        it.setCategoryId(req.getCategoryId());
        it.setLocationText(req.getLocation());
        it.setStatus("listed");
        it.setCreatedAt(java.time.LocalDateTime.now());
        it.setUpdatedAt(java.time.LocalDateTime.now());
        itemMapper.insert(it);
        if (req.getImages() != null) {
            int i = 0;
            for (String url : req.getImages()) {
                ItemImage img = new ItemImage();
                img.setItemId(it.getId());
                img.setImageUrl(url);
                img.setSortOrder(i++);
                img.setCreatedAt(java.time.LocalDateTime.now());
                itemImageMapper.insert(img);
            }
        }
        // invalidate caches
        evictAllLists();
        return it.getId();
    }

    public long countBySeller(Long sellerId, String status, String q) {
        LambdaQueryWrapper<Item> qw = new LambdaQueryWrapper<Item>()
                .eq(Item::getSellerId, sellerId);
        if (status != null && !status.isEmpty()) {
            qw.eq(Item::getStatus, status);
        }
        if (q != null && !q.isEmpty()) {
            qw.like(Item::getTitle, q);
        }
        return itemMapper.selectCount(qw);
    }

    public java.util.List<java.util.Map<String,Object>> listBySeller(Long sellerId, int limit, int offset, String status, String q) {
        LambdaQueryWrapper<Item> qw = new LambdaQueryWrapper<Item>()
                .eq(Item::getSellerId, sellerId);
        if (status != null && !status.isEmpty()) {
            qw.eq(Item::getStatus, status);
        }
        if (q != null && !q.isEmpty()) {
            qw.like(Item::getTitle, q);
        }
        List<Item> list = itemMapper.selectList(qw.orderByDesc(Item::getUpdatedAt)
                .last("limit " + limit + " offset " + offset));
        return list.stream().map(it -> {
            Map<String,Object> m = new HashMap<>();
            m.put("id", it.getId());
            m.put("title", it.getTitle());
            m.put("description", it.getDescription());
            m.put("price", it.getPrice());
            m.put("currency", it.getCurrency());
            m.put("status", it.getStatus());
            m.put("quantity", it.getQuantity());
            m.put("condition", it.getItemCondition());
            m.put("location", it.getLocationText());
            m.put("categoryId", it.getCategoryId());
            m.put("createdAt", it.getCreatedAt());
            m.put("updatedAt", it.getUpdatedAt());
            // 获取分类名称
            if (it.getCategoryId() != null) {
                Category category = categoryMapper.selectById(it.getCategoryId());
                if (category != null) {
                    m.put("categoryName", category.getName());
                }
            }
            // 获取所有图片（不仅仅是封面）
            List<ItemImage> imgs = itemImageMapper.selectList(new LambdaQueryWrapper<ItemImage>()
                    .eq(ItemImage::getItemId, it.getId())
                    .orderByAsc(ItemImage::getSortOrder));
            m.put("image", imgs.isEmpty() ? null : imgs.get(0).getImageUrl());
            m.put("images", imgs.stream().map(ItemImage::getImageUrl).collect(Collectors.toList()));
            // 计算已售数量（通过订单统计，排除已取消和退款订单）
            int soldQuantity = 0;
            try {
                // 先找到包含该商品的所有订单项
                List<org.hfut.work.entity.OrderItem> allOrderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<org.hfut.work.entity.OrderItem>()
                        .eq(org.hfut.work.entity.OrderItem::getItemId, it.getId())
                );
                if (!allOrderItems.isEmpty()) {
                    // 获取这些订单项对应的订单ID
                    List<Long> orderIds = allOrderItems.stream()
                        .map(org.hfut.work.entity.OrderItem::getOrderId)
                        .distinct()
                        .collect(Collectors.toList());
                    // 查询这些订单，只统计非取消、非退款的订单
                    List<org.hfut.work.entity.Order> validOrders = orderMapper.selectList(
                        new LambdaQueryWrapper<org.hfut.work.entity.Order>()
                            .in(org.hfut.work.entity.Order::getId, orderIds)
                            .notIn(org.hfut.work.entity.Order::getStatus, java.util.Arrays.asList("cancelled", "refunded"))
                    );
                    java.util.Set<Long> validOrderIdSet = validOrders.stream()
                        .map(org.hfut.work.entity.Order::getId)
                        .collect(java.util.stream.Collectors.toSet());
                    // 只统计有效订单的订单项数量
                    soldQuantity = allOrderItems.stream()
                        .filter(oi -> validOrderIdSet.contains(oi.getOrderId()))
                        .mapToInt(org.hfut.work.entity.OrderItem::getQuantity)
                        .sum();
                }
            } catch (Exception e) {
                // 如果计算失败，已售数量设为0
                soldQuantity = 0;
            }
            m.put("soldQuantity", soldQuantity);
            return m;
        }).collect(java.util.stream.Collectors.toList());
    }

    public void updateItemBasic(Long sellerId, Long id, java.util.Map<String,Object> body) {
        Item it = itemMapper.selectById(id);
        if (it == null || !it.getSellerId().equals(sellerId)) throw new IllegalArgumentException("商品不存在");
        if (body.containsKey("title")) it.setTitle(String.valueOf(body.get("title")));
        if (body.containsKey("description")) it.setDescription((String) body.get("description"));
        if (body.containsKey("price")) it.setPrice(new java.math.BigDecimal(String.valueOf(body.get("price"))));
        if (body.containsKey("quantity")) it.setQuantity(Integer.valueOf(String.valueOf(body.get("quantity"))));
        if (body.containsKey("status")) it.setStatus(String.valueOf(body.get("status")));
        if (body.containsKey("condition")) it.setItemCondition(String.valueOf(body.get("condition")));
        if (body.containsKey("location")) it.setLocationText(String.valueOf(body.get("location")));
        it.setUpdatedAt(java.time.LocalDateTime.now());
        itemMapper.updateById(it);

        // replace images if provided
        Object imgs = body.get("images");
        if (imgs instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) imgs;
            // delete old
            itemImageMapper.delete(new LambdaQueryWrapper<ItemImage>().eq(ItemImage::getItemId, id));
            int i = 0;
            for (Object o : list) {
                String url = String.valueOf(o);
                if (url == null || url.isEmpty()) continue;
                ItemImage img = new ItemImage();
                img.setItemId(id);
                img.setImageUrl(url);
                img.setSortOrder(i++);
                img.setCreatedAt(java.time.LocalDateTime.now());
                itemImageMapper.insert(img);
            }
        }
        // invalidate caches
        evictItemDetail(id);
        evictAllLists();
    }

    public void deleteItem(Long sellerId, Long id) {
        Item it = itemMapper.selectById(id);
        if (it == null || !it.getSellerId().equals(sellerId)) throw new IllegalArgumentException("商品不存在");
        itemMapper.deleteById(id);
        // images will remain (logical FK); can be cleaned if needed
        evictItemDetail(id);
        evictAllLists();
    }
}


