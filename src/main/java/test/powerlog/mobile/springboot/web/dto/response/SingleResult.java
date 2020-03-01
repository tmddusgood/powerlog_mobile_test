package test.powerlog.mobile.springboot.web.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel
public class SingleResult<T> extends CommonResult {
    @ApiModelProperty(value = "응답 결과")
    private List<T> resultData;
}
