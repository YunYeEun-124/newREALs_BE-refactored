package newREALs.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO<T> {
    //응답 공통 구조 정의
    private boolean success;
    private String code;
    private String message;
    private T data;

    public static <T> ApiResponseDTO<T> success(String message, T data){
        return new ApiResponseDTO<>(true,"S200",message,data);
    }

    public static <T> ApiResponseDTO<T> failure(String code, String message){
        return new ApiResponseDTO<>(false,code,message,null);
    }
}
