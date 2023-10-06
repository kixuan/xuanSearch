package com.xuan.springbootinit.dataSource;

import com.xuan.springbootinit.model.enums.SearchTypeEnum;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


// 注册器模式（本质也是单例）
// 提前通过⼀个 map 或者其他类型存储好后⾯需要调⽤的对象
// 效果：替代了 if else，使得代码更加简洁，维护性更强
@Component
public class DataSourceRegistry {

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    private Map<String, DataSource<T>> typeDataSourceMap;


    // 当依赖注入完成后用于执行初始化的方法，并且只会被执行一次
    @PostConstruct
    public void doInit() {
        // 必须要初始化，否则会报错
        typeDataSourceMap = new HashMap() {{
            put(SearchTypeEnum.POST.getValue(), postDataSource);
            put(SearchTypeEnum.USER.getValue(), userDataSource);
            put(SearchTypeEnum.PICTURE.getValue(), pictureDataSource);
        }};
    }

    public DataSource getDataSourceByType(String type) {
        if (typeDataSourceMap == null) {
            return null;
        }
        return typeDataSourceMap.get(type);
    }
}