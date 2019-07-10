package com.test.redis.common;

/**
 * @author moke
 */
public interface Lock {
    void lock();

    boolean tryLock();

    void unLock();

    boolean tryUnLock();
}
