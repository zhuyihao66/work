package org.hfut.work.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hfut.work.entity.*;
import org.hfut.work.mapper.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FavoriteService {
    
    @Resource
    private FavoriteMapper favoriteMapper;
    
    @Resource
    private FavoriteStoreMapper favoriteStoreMapper;
    
    @Resource
    private BrowseHistoryMapper browseHistoryMapper;
    
    @Resource
    private ItemMapper itemMapper;
    
    @Resource
    private ItemImageMapper itemImageMapper;
    
    @Resource
    private UserMapper userMapper;

    // ========== 宝贝收藏相关 ==========
    
    /**
     * 添加/取消收藏商品
     */
    public boolean toggleFavorite(Long userId, Long itemId) {
        Favorite existing = favoriteMapper.selectOne(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId)
        );
        if (existing != null) {
            // 取消收藏
            favoriteMapper.delete(
                new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getUserId, userId)
                    .eq(Favorite::getItemId, itemId)
            );
            return false;
        } else {
            // 添加收藏
            Favorite fav = new Favorite();
            fav.setUserId(userId);
            fav.setItemId(itemId);
            fav.setCreatedAt(LocalDateTime.now());
            favoriteMapper.insert(fav);
            return true;
        }
    }
    
    /**
     * 检查是否已收藏
     */
    public boolean isFavorited(Long userId, Long itemId) {
        return favoriteMapper.selectOne(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId)
        ) != null;
    }
    
    /**
     * 获取用户收藏的商品列表
     */
    public List<Map<String, Object>> getFavoriteItems(Long userId, int limit, int offset) {
        List<Favorite> favorites = favoriteMapper.selectList(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt)
                .last("limit " + limit + " offset " + offset)
        );
        
        if (favorites.isEmpty()) {
            return List.of();
        }
        
        List<Long> itemIds = favorites.stream()
            .map(Favorite::getItemId)
            .collect(Collectors.toList());
        
        // 查询商品详情
        List<Item> items = itemMapper.selectBatchIds(itemIds);
        Map<Long, Item> itemMap = items.stream()
            .collect(Collectors.toMap(Item::getId, item -> item));
        
        return favorites.stream()
            .map(fav -> {
                Item item = itemMap.get(fav.getItemId());
                if (item == null) {
                    return null;
                }
                
                Map<String, Object> m = new HashMap<>();
                m.put("id", item.getId());
                m.put("title", item.getTitle());
                m.put("description", item.getDescription());
                m.put("price", item.getPrice());
                m.put("currency", item.getCurrency());
                m.put("status", item.getStatus());
                m.put("condition", item.getItemCondition());
                m.put("favoritedAt", fav.getCreatedAt());
                
                // 获取封面图片
                List<ItemImage> imgs = itemImageMapper.selectList(
                    new LambdaQueryWrapper<ItemImage>()
                        .eq(ItemImage::getItemId, item.getId())
                        .orderByAsc(ItemImage::getSortOrder)
                        .last("limit 1")
                );
                m.put("image", imgs.isEmpty() ? null : imgs.get(0).getImageUrl());
                
                return m;
            })
            .filter(m -> m != null)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取收藏数量
     */
    public long getFavoriteCount(Long userId) {
        return favoriteMapper.selectCount(
            new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
        );
    }

    // ========== 收藏的店相关 ==========
    
    /**
     * 添加/取消收藏店铺
     */
    public boolean toggleFavoriteStore(Long userId, Long sellerId) {
        FavoriteStore existing = favoriteStoreMapper.selectOne(
            new LambdaQueryWrapper<FavoriteStore>()
                .eq(FavoriteStore::getUserId, userId)
                .eq(FavoriteStore::getSellerId, sellerId)
        );
        if (existing != null) {
            // 取消收藏
            favoriteStoreMapper.delete(
                new LambdaQueryWrapper<FavoriteStore>()
                    .eq(FavoriteStore::getUserId, userId)
                    .eq(FavoriteStore::getSellerId, sellerId)
            );
            return false;
        } else {
            // 添加收藏
            FavoriteStore fs = new FavoriteStore();
            fs.setUserId(userId);
            fs.setSellerId(sellerId);
            fs.setCreatedAt(LocalDateTime.now());
            favoriteStoreMapper.insert(fs);
            return true;
        }
    }
    
    /**
     * 检查是否已收藏店铺
     */
    public boolean isStoreFavorited(Long userId, Long sellerId) {
        return favoriteStoreMapper.selectOne(
            new LambdaQueryWrapper<FavoriteStore>()
                .eq(FavoriteStore::getUserId, userId)
                .eq(FavoriteStore::getSellerId, sellerId)
        ) != null;
    }
    
    /**
     * 获取用户收藏的店铺列表
     */
    public List<Map<String, Object>> getFavoriteStores(Long userId, int limit, int offset) {
        List<FavoriteStore> favoriteStores = favoriteStoreMapper.selectList(
            new LambdaQueryWrapper<FavoriteStore>()
                .eq(FavoriteStore::getUserId, userId)
                .orderByDesc(FavoriteStore::getCreatedAt)
                .last("limit " + limit + " offset " + offset)
        );
        
        if (favoriteStores.isEmpty()) {
            return List.of();
        }
        
        List<Long> sellerIds = favoriteStores.stream()
            .map(FavoriteStore::getSellerId)
            .collect(Collectors.toList());
        
        // 查询卖家信息
        List<User> sellers = userMapper.selectBatchIds(sellerIds);
        Map<Long, User> sellerMap = sellers.stream()
            .collect(Collectors.toMap(User::getId, seller -> seller));
        
        return favoriteStores.stream()
            .map(fs -> {
                User seller = sellerMap.get(fs.getSellerId());
                if (seller == null) {
                    return null;
                }
                
                Map<String, Object> m = new HashMap<>();
                m.put("id", seller.getId());
                m.put("displayName", seller.getDisplayName());
                m.put("avatarUrl", seller.getAvatarUrl());
                m.put("favoritedAt", fs.getCreatedAt());
                
                // 统计该卖家的商品数量
                long itemCount = itemMapper.selectCount(
                    new LambdaQueryWrapper<Item>()
                        .eq(Item::getSellerId, seller.getId())
                        .eq(Item::getStatus, "listed")
                );
                m.put("itemCount", itemCount);
                
                return m;
            })
            .filter(m -> m != null)
            .collect(Collectors.toList());
    }
    
    /**
     * 获取收藏店铺数量
     */
    public long getFavoriteStoreCount(Long userId) {
        return favoriteStoreMapper.selectCount(
            new LambdaQueryWrapper<FavoriteStore>()
                .eq(FavoriteStore::getUserId, userId)
        );
    }

    // ========== 我的足迹相关 ==========
    
    /**
     * 记录浏览历史
     */
    public void recordBrowseHistory(Long userId, Long itemId) {
        // 检查是否已存在（最近30天内）
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        BrowseHistory existing = browseHistoryMapper.selectOne(
            new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
                .eq(BrowseHistory::getItemId, itemId)
                .ge(BrowseHistory::getViewedAt, cutoff)
                .orderByDesc(BrowseHistory::getViewedAt)
                .last("limit 1")
        );
        
        if (existing == null) {
            // 创建新记录
            BrowseHistory bh = new BrowseHistory();
            bh.setUserId(userId);
            bh.setItemId(itemId);
            bh.setViewedAt(LocalDateTime.now());
            browseHistoryMapper.insert(bh);
        } else {
            // 更新浏览时间
            existing.setViewedAt(LocalDateTime.now());
            browseHistoryMapper.updateById(existing);
        }
        
        // 清理旧记录（保留最近100条）
        List<BrowseHistory> allHistory = browseHistoryMapper.selectList(
            new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
                .orderByDesc(BrowseHistory::getViewedAt)
        );
        if (allHistory.size() > 100) {
            List<Long> idsToDelete = allHistory.subList(100, allHistory.size()).stream()
                .map(BrowseHistory::getId)
                .collect(Collectors.toList());
            for (Long id : idsToDelete) {
                browseHistoryMapper.deleteById(id);
            }
        }
    }
    
    /**
     * 获取浏览历史
     */
    public List<Map<String, Object>> getBrowseHistory(Long userId, int limit, int offset) {
        List<BrowseHistory> history = browseHistoryMapper.selectList(
            new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
                .orderByDesc(BrowseHistory::getViewedAt)
                .last("limit " + limit + " offset " + offset)
        );
        
        if (history.isEmpty()) {
            return List.of();
        }
        
        List<Long> itemIds = history.stream()
            .map(BrowseHistory::getItemId)
            .collect(Collectors.toList());
        
        // 查询商品详情
        List<Item> items = itemMapper.selectBatchIds(itemIds);
        Map<Long, Item> itemMap = items.stream()
            .collect(Collectors.toMap(Item::getId, item -> item));
        
        return history.stream()
            .map(bh -> {
                Item item = itemMap.get(bh.getItemId());
                if (item == null) {
                    return null;
                }
                
                Map<String, Object> m = new HashMap<>();
                m.put("id", item.getId());
                m.put("title", item.getTitle());
                m.put("description", item.getDescription());
                m.put("price", item.getPrice());
                m.put("currency", item.getCurrency());
                m.put("status", item.getStatus());
                m.put("condition", item.getItemCondition());
                m.put("viewedAt", bh.getViewedAt());
                
                // 获取封面图片
                List<ItemImage> imgs = itemImageMapper.selectList(
                    new LambdaQueryWrapper<ItemImage>()
                        .eq(ItemImage::getItemId, item.getId())
                        .orderByAsc(ItemImage::getSortOrder)
                        .last("limit 1")
                );
                m.put("image", imgs.isEmpty() ? null : imgs.get(0).getImageUrl());
                
                return m;
            })
            .filter(m -> m != null)
            .collect(Collectors.toList());
    }
    
    /**
     * 删除浏览历史
     */
    public void deleteBrowseHistory(Long userId, Long itemId) {
        browseHistoryMapper.delete(
            new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
                .eq(BrowseHistory::getItemId, itemId)
        );
    }
    
    /**
     * 清空浏览历史
     */
    public void clearBrowseHistory(Long userId) {
        browseHistoryMapper.delete(
            new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
        );
    }
    
    /**
     * 获取浏览历史数量
     */
    public long getBrowseHistoryCount(Long userId) {
        return browseHistoryMapper.selectCount(
            new LambdaQueryWrapper<BrowseHistory>()
                .eq(BrowseHistory::getUserId, userId)
        );
    }
}

