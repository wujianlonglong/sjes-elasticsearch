package sjes.elasticsearch.domain;

import java.io.Serializable;

/**
 * 分页信息
 */
public class Pageable implements Serializable {

    private static final long serialVersionUID = -3930180379790344299L;

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE_NUMBER = 0;

    /**
     * 默认每页记录数
     */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大每页记录数
     */
    private static final int MAX_PAGE_SIZE = 1000;

    /**
     * 页码
     */
    private int page = DEFAULT_PAGE_NUMBER;

    /**
     * 每页记录数
     */
    private int size = DEFAULT_PAGE_SIZE;

    /**
     * 初始化一个新创建的Pageable对象
     */
    public Pageable() {
    }

    /**
     * 初始化一个新创建的Pageable对象
     *
     * @param page 页码
     * @param size 每页记录数
     */
    public Pageable(Integer page, Integer size) {
        if (page != null && page >= 1) {
            this.page = page;
        }
        if (size != null && size >= 1 && size <= MAX_PAGE_SIZE) {
            this.size = size;
        }
    }

    /**
     * 获取页码
     *
     * @return 页码
     */
    public int getPage() {
        return page;
    }

    /**
     * 设置页码
     *
     * @param page 页码
     */
    public void setPage(int page) {
        if (page < 1) {
            page = DEFAULT_PAGE_NUMBER;
        }
        this.page = page;
    }

    /**
     * 获取每页记录数
     *
     * @return 每页记录数
     */
    public int getSize() {
        return size;
    }

    /**
     * 设置每页记录数
     *
     * @param size 每页记录数
     */
    public void setSize(int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            size = DEFAULT_PAGE_SIZE;
        }
        this.size = size;
    }


}