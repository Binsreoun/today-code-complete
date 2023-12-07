package sssdev.tcc.domain.user.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static sssdev.tcc.global.execption.ErrorCode.CHECK_USER;
import static sssdev.tcc.global.execption.ErrorCode.NOT_EXIST_USER;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import sssdev.tcc.domain.user.dto.request.ProfileUpdateRequest;
import sssdev.tcc.domain.user.dto.response.ProfileResponse;
import sssdev.tcc.domain.user.service.UserService;
import sssdev.tcc.global.common.dto.LoginUser;
import sssdev.tcc.global.execption.ServiceException;
import sssdev.tcc.support.ControllerTest;

@DisplayName("유저 API 서비스")
class UserControllerTest extends ControllerTest {

    @MockBean
    UserService userService;

    @Nested
    @DisplayName("프로필 단건 조회")
    class ProfileGet {

        @DisplayName("성공 케이스")
        @Test
        void success() throws Exception {
            // given
            var userId = 1L;
            var response = new ProfileResponse("test", 100, 200, "/api/test/image.png",
                "description");
            given(userService.getProfile(userId)).willReturn(response);
            // when // then
            mockMvc.perform(get("/api/users/{userId}/profile", userId))
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.code").value("200"),
                    jsonPath("$.message").value("성공했습니다."),
                    jsonPath("$.data.nickname").value(response.nickname()),
                    jsonPath("$.data.followerCount").value(response.followerCount()),
                    jsonPath("$.data.followingCount").value(response.followingCount()),
                    jsonPath("$.data.profileImageUrl").value(response.profileImageUrl()),
                    jsonPath("$.data.description").value(response.description())
                );
        }

        @DisplayName("사용자가 존재하지 않습니다.")
        @Test
        void fail_1() throws Exception {
            // given
            var userId = 1L;
            given(userService.getProfile(userId)).willThrow(new ServiceException(NOT_EXIST_USER));
            // when // then
            mockMvc.perform(get("/api/users/{userId}/profile", userId))
                .andDo(print())
                .andExpectAll(
                    status().isBadRequest(),
                    jsonPath("$.code").value("1000"),
                    jsonPath("$.message").value("사용자가 없습니다.")
                );
        }
    }

    @Nested
    @DisplayName("프로필 수정")
    class ProfileUpdate {

        @DisplayName("성공 케이스")
        @Test
        void success() throws Exception {
            // given
            var userId = 1L;
            var response = new ProfileResponse("test2", 100, 200, "/api/test/image.png",
                "description2");
            var requst = new ProfileUpdateRequest("test2", "description2");
            var sesstion = new LoginUser(userId);

            String json = objectMapper.writeValueAsString(requst);

            given(userService.updateProfile(requst, userId)).willReturn(response);
            // when // then
            mockMvc.perform(patch("/api/users/profile")
                    .content(json)
                    .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                    .accept(MediaType.APPLICATION_JSON)
                    .sessionAttr("login_user", sesstion)

                )
                .andDo(print())
                .andExpectAll(
                    status().isOk(),
                    jsonPath("$.code").value("200"),
                    jsonPath("$.message").value("성공했습니다."),
                    jsonPath("$.data.nickname").value(response.nickname()),
                    jsonPath("$.data.followerCount").value(response.followerCount()),
                    jsonPath("$.data.followingCount").value(response.followingCount()),
                    jsonPath("$.data.profileImageUrl").value(response.profileImageUrl()),
                    jsonPath("$.data.description").value(response.description())
                );
        }

        @DisplayName("실패 케이스")
        @Test
        void fail_1() throws Exception {

            var userId = 1L;
            var requst = new ProfileUpdateRequest("test2", null);
            var sesstion = new LoginUser(userId);

            String json = objectMapper.writeValueAsString(requst);

            given(userService.updateProfile(requst, userId)).willThrow(
                new ServiceException(CHECK_USER));
            // when // then
            mockMvc.perform(patch("/api/users/profile")
                    .content(json)
                    .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
                    .accept(MediaType.APPLICATION_JSON)
                    .sessionAttr("login_user", sesstion)

                )
                .andDo(print())
                .andExpectAll(
                    status().isBadRequest(),
                    jsonPath("$.code").value("1001"),
                    jsonPath("$.message").value("본인이 아닙니다.")
                );
        }
    }
}