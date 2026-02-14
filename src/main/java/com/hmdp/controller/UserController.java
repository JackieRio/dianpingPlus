package com.hmdp.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发送手机验证码
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone) {
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone);
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm){
        // 实现登录功能
        return userService.login(loginForm);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result logout(){
        try {
            // 1.获取请求头中的token
            String token = request.getHeader("authorization");

            // 2.如果token为空，表示未登录，直接返回成功
            if (StrUtil.isBlank(token)) {
                // 清理ThreadLocal中的用户信息以防万一
                UserHolder.removeUser();
                return Result.ok("用户未登录，无需登出");
            }

            // 3.构建Redis中存储用户信息的key
            String key = RedisConstants.LOGIN_USER_KEY + token;

            // 4.删除Redis中的用户信息
            Boolean deleted = stringRedisTemplate.delete(key);

            // 5.清除当前线程ThreadLocal中的用户信息
            UserHolder.removeUser();

            // 6.返回登出成功
            String message = deleted != null && deleted ? "登出成功" : "登出成功（用户信息已不存在）";
            return Result.ok(message);

        } catch (Exception e) {
            log.error("登出异常", e);
            // 出现异常也要清理ThreadLocal
            UserHolder.removeUser();
            return Result.fail("登出失败");
        }
    }

    @GetMapping("/me")
    public Result me(){
        // 获取当前登录的用户并返回
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return Result.ok(info);
    }

    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return Result.ok(userDTO);
    }

    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }
}