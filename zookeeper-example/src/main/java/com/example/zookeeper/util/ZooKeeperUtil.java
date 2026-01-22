package com.example.zookeeper.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ZooKeeper 工具类
 * 封装常用的 ZooKeeper 操作
 */
@Slf4j
@Component
public class ZooKeeperUtil {

    @Autowired
    private CuratorFramework curatorFramework;

    /**
     * 创建节点
     * @param path 节点路径
     * @param data 节点数据
     * @param mode 节点类型
     * @return 创建的节点路径
     */
    public String createNode(String path, String data, CreateMode mode) {
        try {
            String createdPath = curatorFramework.create()
                    .creatingParentsIfNeeded()
                    .withMode(mode)
                    .forPath(path, data.getBytes(StandardCharsets.UTF_8));
            log.debug("创建节点成功: path={}, data={}, mode={}", createdPath, data, mode);
            return createdPath;
        } catch (Exception e) {
            log.error("创建节点失败: path={}", path, e);
            throw new RuntimeException("创建节点失败: " + path, e);
        }
    }

    /**
     * 创建持久化节点
     * @param path 节点路径
     * @param data 节点数据
     * @return 创建的节点路径
     */
    public String createPersistentNode(String path, String data) {
        return createNode(path, data, CreateMode.PERSISTENT);
    }

    /**
     * 创建临时节点
     * @param path 节点路径
     * @param data 节点数据
     * @return 创建的节点路径
     */
    public String createEphemeralNode(String path, String data) {
        return createNode(path, data, CreateMode.EPHEMERAL);
    }

    /**
     * 获取节点数据
     * @param path 节点路径
     * @return 节点数据
     */
    public String getData(String path) {
        try {
            byte[] data = curatorFramework.getData().forPath(path);
            String result = new String(data, StandardCharsets.UTF_8);
            log.debug("获取节点数据成功: path={}, data={}", path, result);
            return result;
        } catch (Exception e) {
            log.error("获取节点数据失败: path={}", path, e);
            throw new RuntimeException("获取节点数据失败: " + path, e);
        }
    }

    /**
     * 设置节点数据
     * @param path 节点路径
     * @param data 节点数据
     */
    public void setData(String path, String data) {
        try {
            curatorFramework.setData().forPath(path, data.getBytes(StandardCharsets.UTF_8));
            log.debug("设置节点数据成功: path={}, data={}", path, data);
        } catch (Exception e) {
            log.error("设置节点数据失败: path={}", path, e);
            throw new RuntimeException("设置节点数据失败: " + path, e);
        }
    }

    /**
     * 检查节点是否存在
     * @param path 节点路径
     * @return 是否存在
     */
    public boolean exists(String path) {
        try {
            Stat stat = curatorFramework.checkExists().forPath(path);
            boolean exists = stat != null;
            log.debug("检查节点存在: path={}, exists={}", path, exists);
            return exists;
        } catch (Exception e) {
            log.error("检查节点存在失败: path={}", path, e);
            return false;
        }
    }

    /**
     * 获取节点状态信息
     * @param path 节点路径
     * @return 节点状态，如果节点不存在返回 null
     */
    public Stat getStat(String path) {
        try {
            Stat stat = curatorFramework.checkExists().forPath(path);
            log.debug("获取节点状态: path={}, stat={}", path, stat != null ? "exists" : "not exists");
            return stat;
        } catch (Exception e) {
            log.error("获取节点状态失败: path={}", path, e);
            return null;
        }
    }

    /**
     * 获取子节点列表
     * @param path 节点路径
     * @return 子节点列表
     */
    public List<String> getChildren(String path) {
        try {
            List<String> children = curatorFramework.getChildren().forPath(path);
            log.debug("获取子节点列表成功: path={}, children={}", path, children);
            return children;
        } catch (Exception e) {
            log.error("获取子节点列表失败: path={}", path, e);
            throw new RuntimeException("获取子节点列表失败: " + path, e);
        }
    }

    /**
     * 删除节点
     * @param path 节点路径
     */
    public void deleteNode(String path) {
        try {
            curatorFramework.delete()
                    .deletingChildrenIfNeeded()
                    .forPath(path);
            log.debug("删除节点成功: path={}", path);
        } catch (Exception e) {
            log.error("删除节点失败: path={}", path, e);
            throw new RuntimeException("删除节点失败: " + path, e);
        }
    }

    /**
     * 删除节点（不删除子节点）
     * @param path 节点路径
     */
    public void deleteNodeOnly(String path) {
        try {
            curatorFramework.delete().forPath(path);
            log.debug("删除节点成功（不删除子节点）: path={}", path);
        } catch (Exception e) {
            log.error("删除节点失败: path={}", path, e);
            throw new RuntimeException("删除节点失败: " + path, e);
        }
    }

    /**
     * 监听节点数据变化
     * @param path 节点路径
     * @param listener 监听器
     * @return NodeCache 实例，需要调用者管理生命周期
     */
    public NodeCache watchNode(String path, NodeCacheListener listener) {
        try {
            NodeCache nodeCache = new NodeCache(curatorFramework, path);
            nodeCache.getListenable().addListener(listener);
            nodeCache.start();
            log.debug("开始监听节点: path={}", path);
            return nodeCache;
        } catch (Exception e) {
            log.error("监听节点失败: path={}", path, e);
            throw new RuntimeException("监听节点失败: " + path, e);
        }
    }

    /**
     * 监听子节点变化
     * @param path 节点路径
     * @param listener 监听器
     * @return PathChildrenCache 实例，需要调用者管理生命周期
     */
    public PathChildrenCache watchChildren(String path, PathChildrenCacheListener listener) {
        try {
            PathChildrenCache childrenCache = new PathChildrenCache(curatorFramework, path, true);
            childrenCache.getListenable().addListener(listener);
            childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            log.debug("开始监听子节点: path={}", path);
            return childrenCache;
        } catch (Exception e) {
            log.error("监听子节点失败: path={}", path, e);
            throw new RuntimeException("监听子节点失败: " + path, e);
        }
    }

    /**
     * 检查连接状态
     * @return 是否已连接
     */
    public boolean isConnected() {
        return curatorFramework.getZookeeperClient().isConnected();
    }

    /**
     * 获取 CuratorFramework 实例（用于高级操作）
     * @return CuratorFramework 实例
     */
    public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }
}
