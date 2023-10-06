package com.xuan.springbootinit.dataSource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.springbootinit.model.entity.Picture;
import com.xuan.springbootinit.service.PictureService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

// 注意这里必须实现 DataSource 接口
@Service
public class PictureDataSource implements DataSource<Picture> {
    @Resource
    private PictureService pictureService;

    @Override
    public Page<Picture> doSearch(String searchText, long pageNum, long pageSize) {
        return pictureService.searchPicture(searchText, pageNum, pageSize);
    }
}