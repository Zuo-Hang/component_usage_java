package com.example.zookeeper.service;

import com.example.zookeeper.util.ZooKeeperUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ZooKeeper服务类
 * 演示基本的ZooKeeper操作
 * 使用 ZooKeeperUtil 工具类封装操作
 */
@Slf4j
@Service
public class ZooKeeperService {

    @Autowired
    private ZooKeeperUtil zookeeperUtil;

    /**
     * 创建节点
     * @param path 节点路径
     * @param data 节点数据
     * @param mode 节点类型（持久化、临时等）
     * @return 创建的节点路径
     */
    public String createNode(String path, String data, CreateMode mode) {
        log.info("创建节点: path={}, data={}, mode={}", path, data, mode);
        return zookeeperUtil.createNode(path, data, mode);
    }

    /**
     * 创建持久化节点
     * @param path 节点路径
     * @param data 节点数据
     * @return 创建的节点路径
     */
    public String createPersistentNode(String path, String data) {
        log.info("创建持久化节点: path={}, data={}", path, data);
        return zookeeperUtil.createPersistentNode(path, data);
    }

    /**
     * 创建临时节点
     * @param path 节点路径
     * @param data 节点数据
     * @return 创建的节点路径
     */
    public String createEphemeralNode(String path, String data) {
        log.info("创建临时节点: path={}, data={}", path, data);
        return zookeeperUtil.createEphemeralNode(path, data);
    }

    /**
     * 获取节点数据
     * @param path 节点路径
     * @return 节点数据
     */
    public String getData(String path) {
        log.info("获取节点数据: path={}", path);
        return zookeeperUtil.getData(path);
    }

    /**
     * 设置节点数据
     * @param path 节点路径
     * @param data 节点数据
     */
    public void setData(String path, String data) {
        log.info("设置节点数据: path={}, data={}", path, data);
        zookeeperUtil.setData(path, data);
    }

    /**
     * 检查节点是否存在
     * @param path 节点路径
     * @return 是否存在
     */
    public boolean exists(String path) {
        log.info("检查节点存在: path={}", path);
        return zookeeperUtil.exists(path);
    }

    /**
     * 获取子节点列表
     * @param path 节点路径
     * @return 子节点列表
     */
    public List<String> getChildren(String path) {
        log.info("获取子节点列表: path={}", path);
        return zookeeperUtil.getChildren(path);
    }

    /**
     * 删除节点
     * @param path 节点路径
     */
    public void deleteNode(String path) {
        log.info("删除节点: path={}", path);
        zookeeperUtil.deleteNode(path);
    }

    /**
     * 监听节点数据变化
     * @param path 节点路径
     * @param listener 监听器
     * @return NodeCache 实例，需要调用者管理生命周期
     */
    public NodeCache watchNode(String path, NodeCacheListener listener) {
        log.info("开始监听节点: path={}", path);
        return zookeeperUtil.watchNode(path, listener);
    }

    /**
     * 监听子节点变化
     * @param path 节点路径
     * @param listener 监听器
     * @return PathChildrenCache 实例，需要调用者管理生命周期
     */
    public PathChildrenCache watchChildren(String path, PathChildrenCacheListener listener) {
        log.info("开始监听子节点: path={}", path);
        return zookeeperUtil.watchChildren(path, listener);
    }
}
