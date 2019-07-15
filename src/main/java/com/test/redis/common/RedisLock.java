package com.test.redis.common;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

/**
 * @author moke
 */
@Component
public class RedisLock implements Lock {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private final String LOCK_KEY = "lock";
    private final String LUA_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    private final ThreadLocal<String> uuids = new ThreadLocal<>();


    @Override
    public boolean tryLock() {
        String uuid = uuids.get();
        if(uuid == null){
            // 使用雪花算法更能确保唯一
            uuid = UUID.randomUUID().toString().replace("-", "");
            uuids.set(uuid);
        }
        String finalUuid = uuid;
        return stringRedisTemplate.execute( (RedisConnection connection) ->
                connection.set(LOCK_KEY.getBytes(), finalUuid.getBytes(), Expiration.seconds(5), RedisStringCommands.SetOption.SET_IF_ABSENT)
        );
    }

    @Override
    public boolean tryUnLock() {
        String uuid = uuids.get();
        if(uuid == null){
            return false;
        }
        Boolean result = stringRedisTemplate.<Boolean>execute(RedisScript.of(LUA_SCRIPT, Boolean.class), Collections.singletonList(LOCK_KEY), uuid);
        if(result){
            uuids.remove();
        }
        return result;
    }

    @Override
    public void lock() {
        while(!tryLock()){

        }
    }

    @Override
    public void unLock() {
        LocalDateTime start = LocalDateTime.now();
        while(!tryUnLock()){
            LocalDateTime end = LocalDateTime.now();
            long between = Duration.between(start, end).toMillis();
            if(between > 1000 * 1){
                break;
            }
        }
    }
}
