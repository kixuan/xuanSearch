package com.yupi.springbootinit.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.model.dto.picture.PictureQueryRequest;
import com.yupi.springbootinit.model.dto.post.PostQueryRequest;
import com.yupi.springbootinit.model.dto.search.SearchRequest;
import com.yupi.springbootinit.model.dto.user.UserQueryRequest;
import com.yupi.springbootinit.model.entity.Picture;
import com.yupi.springbootinit.model.enums.SearchVO;
import com.yupi.springbootinit.model.vo.PostVO;
import com.yupi.springbootinit.model.vo.UserVO;
import com.yupi.springbootinit.service.PictureService;
import com.yupi.springbootinit.service.PostService;
import com.yupi.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * 图片接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;

    @Resource
    private PictureService pictureService;


    @PostMapping("/all")
    public BaseResponse<SearchVO> searchAll(@RequestBody SearchRequest searchQueryRequest, HttpServletRequest request) {
        // 2.0版本：进行并发查询
        // 但注意并不一定就会变快，因为接口的不稳定性，可能会导致某个接口的响应时间过长，从而导致整体的响应时间变长
        String searchText = searchQueryRequest.getSearchText();
        CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
            UserQueryRequest userQueryRequest = new UserQueryRequest();
            userQueryRequest.setUserName(searchText);
            return userService.listUserVOByPage(userQueryRequest);
        });
        CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
            PostQueryRequest postQueryRequest = new PostQueryRequest();
            postQueryRequest.setSearchText(searchText);
            return postService.listPostVOByPage(postQueryRequest, request);
        });
        CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> {
            PictureQueryRequest pictureQueryRequest = new PictureQueryRequest();
            pictureQueryRequest.setSearchText(searchText);
            return pictureService.searchPicture(searchText, 1, 10);
        });
        CompletableFuture.allOf(userTask, postTask, pictureTask).join();
        try {
            return ResultUtils.success(SearchVO.builder().userList(userTask.get().getRecords())
                    .pictureList(pictureTask.get().getRecords())
                    .postList(postTask.get().getRecords()).build());
        } catch (Exception e) {
            log.error("查询异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常");
        }
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
    }

}
