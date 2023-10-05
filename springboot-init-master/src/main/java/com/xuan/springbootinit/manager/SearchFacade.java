package com.xuan.springbootinit.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.springbootinit.common.ErrorCode;
import com.xuan.springbootinit.dataSource.*;
import com.xuan.springbootinit.exception.BusinessException;
import com.xuan.springbootinit.exception.ThrowUtils;
import com.xuan.springbootinit.model.dto.picture.PictureQueryRequest;
import com.xuan.springbootinit.model.dto.post.PostQueryRequest;
import com.xuan.springbootinit.model.dto.search.SearchRequest;
import com.xuan.springbootinit.model.dto.user.UserQueryRequest;
import com.xuan.springbootinit.model.entity.Picture;
import com.xuan.springbootinit.model.enums.SearchTypeEnum;
import com.xuan.springbootinit.model.enums.SearchVO;
import com.xuan.springbootinit.model.vo.PostVO;
import com.xuan.springbootinit.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * 搜索门面
 */
@Component
@Slf4j
public class SearchFacade {

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    @Resource
    private DataSourceRegistry dataSourceRegistry;


    public SearchVO searchAll(@RequestBody SearchRequest searchQueryRequest, HttpServletRequest request) {
        String type = searchQueryRequest.getType();
        String searchText = searchQueryRequest.getSearchText();
        long current = searchQueryRequest.getCurrent();
        long pageSize = searchQueryRequest.getPageSize();

        // 为空的话直接抛出异常
        ThrowUtils.throwIf(StringUtils.isBlank(type), ErrorCode.PARAMS_ERROR);
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);

        if (ObjectUtils.isEmpty(searchTypeEnum)) {
            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
                UserQueryRequest userQueryRequest = new UserQueryRequest();
                userQueryRequest.setUserName(searchText);
                return userDataSource.doSearch(searchText, current, pageSize);
            });
            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
                PostQueryRequest postQueryRequest = new PostQueryRequest();
                postQueryRequest.setSearchText(searchText);
                return postDataSource.doSearch(searchText, current, pageSize);
            });
            CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
                PictureQueryRequest pictureQueryRequest = new PictureQueryRequest();
                pictureQueryRequest.setSearchText(searchText);
                return pictureDataSource.doSearch(searchText, current, pageSize);
            });
            CompletableFuture.allOf(userTask, postTask, pictureTask).join();
            try {
                return SearchVO.builder().userList(userTask.get().getRecords()).pictureList(pictureTask.get().getRecords()).postList(postTask.get().getRecords()).build();
            } catch (Exception e) {
                log.error("查询异常", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
            }
        } else {
            // 简化switch代码
            SearchVO searchVO = new SearchVO();
            DataSource<?> searchDataSource = dataSourceRegistry.getDataSourceByType(type);
            Page<?> page = searchDataSource.doSearch(searchText, current, pageSize);
            searchVO.setDataList(page.getRecords());
            return searchVO;


            // 分类查询，但是要有不足：每个接口的分页查询都要重写一遍，随着type的增加（业务的增加会更加麻烦）  --》  门面模式
            // SearchVO searchVO = new SearchVO();
            // switch (searchTypeEnum) {
            //     case POST:
            //         PostQueryRequest postQueryRequest = new PostQueryRequest();
            //         postQueryRequest.setSearchText(searchText);
            //         Page<PostVO> postVOPage = postDataSource.doSearch(searchText, current, pageSize);
            //         searchVO.setPostList(postVOPage.getRecords());
            //         break;
            //     case USER:
            //         UserQueryRequest userQueryRequest = new UserQueryRequest();
            //         userQueryRequest.setUserName(searchText);
            //         Page<UserVO> userVOPage = userDataSource.doSearch(searchText, current, pageSize);
            //         searchVO.setUserList(userVOPage.getRecords());
            //         break;
            //     case PICTURE:
            //         Page<Picture> picturePage = pictureDataSource.doSearch(searchText, current, pageSize);
            //         searchVO.setPictureList(picturePage.getRecords());
            //         break;
            // }
            // return searchVO;
        }
    }


    // 2.0版本：进行并发查询
    // 但注意并不一定就会变快，因为接口的不稳定性，可能会导致某个接口的响应时间过长，从而导致整体的响应时间变长
    // CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
    //     UserQueryRequest userQueryRequest = new UserQueryRequest();
    //     userQueryRequest.setUserName(searchText);
    //     return userService.listUserVOByPage(userQueryRequest);
    // });
    // CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
    //     PostQueryRequest postQueryRequest = new PostQueryRequest();
    //     postQueryRequest.setSearchText(searchText);
    //     return postService.listPostVOByPage(postQueryRequest, request);
    // });
    // CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
    //     PictureQueryRequest pictureQueryRequest = new PictureQueryRequest();
    //     pictureQueryRequest.setSearchText(searchText);
    //     return pictureService.searchPicture(searchText, 1, 10);
    // });
    // CompletableFuture.allOf(userTask, postTask, pictureTask).join();
    // try {
    //     return ResultUtils.success(SearchVO.builder().userList(userTask.get().getRecords())
    //             .pictureList(pictureTask.get().getRecords())
    //             .postList(postTask.get().getRecords()).build());
    // } catch (Exception e) {
    //     log.error("查询异常", e);
    //     throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
    // }
    // }


    // 初步1.0版本
    // // 测试时间
    // long startTime = System.currentTimeMillis();
    //
    // String searchText = searchQueryRequest.getSearchText();
    // // 用户
    // UserQueryRequest userQueryRequest = new UserQueryRequest();
    // userQueryRequest.setUserName(searchText);
    // Page<UserVO> userVOPage = userService.listUserVOByPage(userQueryRequest);
    //
    // // 图片
    // PictureQueryRequest pictureQueryRequest = new PictureQueryRequest();
    // pictureQueryRequest.setSearchText(searchText);
    // Page<Picture> pictureVOPage = pictureService.searchPicture(searchText, 1, 10);
    //
    // // 帖子
    // PostQueryRequest postQueryRequest = new PostQueryRequest();
    // postQueryRequest.setSearchText(searchText);
    // Page<PostVO> postVOPage = postService.listPostVOByPage(postQueryRequest, request);
    //
    //
    // SearchVO searchVO = new SearchVO();
    // searchVO.setPictureList(pictureVOPage.getRecords());
    // searchVO.setPostList(postVOPage.getRecords());
    // searchVO.setUserList(userVOPage.getRecords());
    // long endTime = System.currentTimeMillis();
    // System.out.println("程序运行时间：" + (endTime - startTime) + "ms");
    // return ResultUtils.success(searchVO);

    // 这一段类似于一个工厂模式，将三个分页查询的结果封装到一个类中
    // return ResultUtils.success(SearchVO.builder().userList(userVOPage.getRecords())
    //         .pictureList(pictureVOPage.getRecords())
    //         .postList(postVOPage.getRecords()).build());
    // }
}
