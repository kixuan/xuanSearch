package com.xuan.springbootinit.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.springbootinit.model.entity.Picture;

// service类
public interface PictureService {

    Page<Picture> searchPicture(String searchText, long pageNum, long pageSize);
}