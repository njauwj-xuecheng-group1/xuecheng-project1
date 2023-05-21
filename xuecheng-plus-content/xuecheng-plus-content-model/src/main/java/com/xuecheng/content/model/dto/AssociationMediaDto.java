package com.xuecheng.content.model.dto;

import lombok.Data;

/**
 * @author: wj
 * @create_time: 2023/5/11 18:45
 * @explain: 绑定媒资
 */
@Data
public class AssociationMediaDto {

    private String fileName;

    private String mediaId;

    private Long teachplanId;

}
