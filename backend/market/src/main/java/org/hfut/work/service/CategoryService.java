package org.hfut.work.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.hfut.work.entity.Category;
import org.hfut.work.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class CategoryService {
    @Resource
    private CategoryMapper categoryMapper;

    public List<Map<String, Object>> tree() {
        List<Category> all = categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getParentId).orderByAsc(Category::getId));
        Map<Long, List<Map<String,Object>>> children = new HashMap<>();
        Map<Long, Map<String,Object>> nodes = new HashMap<>();
        List<Map<String,Object>> roots = new ArrayList<>();
        for (Category c : all) {
            Map<String,Object> n = new HashMap<>();
            n.put("id", c.getId());
            n.put("name", c.getName());
            n.put("slug", c.getSlug());
            nodes.put(c.getId(), n);
            children.computeIfAbsent(c.getId(), k-> new ArrayList<>());
        }
        for (Category c : all) {
            Map<String,Object> n = nodes.get(c.getId());
            List<Map<String,Object>> ch = children.get(c.getId());
            if (ch != null && !ch.isEmpty()) n.put("children", ch);
            if (c.getParentId() == null) roots.add(n);
            else children.computeIfAbsent(c.getParentId(), k-> new ArrayList<>()).add(n);
        }
        return roots;
    }
}


