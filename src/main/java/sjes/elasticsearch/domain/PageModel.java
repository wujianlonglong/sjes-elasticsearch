package sjes.elasticsearch.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 分页
 */
public class PageModel<T> implements Serializable {

    private static final long serialVersionUID = -2053800594583879853L;

    /**
     * 内容
     */
    private final List<T> content = new ArrayList<T>();

    /**
     * 总记录数
     */
    private final long total;

    /**
     * 分页信息
     */
    private final Pageable pageable;

    /**
     * 附带参数
     */
    private Set<Long> categoryIdSet;

    /**
     * 初始化一个新创建的Page对象
     */
    public PageModel() {
        this.total = 0L;
        this.pageable = new Pageable();
    }

    /**
     * @param content  内容
     * @param total    总记录数
     * @param pageable 分页信息
     */
    public PageModel(List<T> content, long total, Pageable pageable) {
        this.content.addAll(content);
        this.total = total;
        this.pageable = pageable;
    }

    /**
     * @param content  内容
     * @param total    总记录数
     * @param categoryIdSet   分类列表
     * @param pageable 分页信息
     */
    public PageModel(List<T> content, long total,Set<Long> categoryIdSet, Pageable pageable) {
        this.content.addAll(content);
        this.total = total;
        this.categoryIdSet = categoryIdSet;
        this.pageable = pageable;
    }

    /**
     * 获取页码
     *
     * @return 页码
     */
    public int getPage() {
        return pageable.getPage();
    }

    /**
     * 获取每页记录数
     *
     * @return 每页记录数
     */
    public int getSize() {
        return pageable.getSize();
    }


    /**
     * 获取总页数
     *
     * @return 总页数
     */
    public int getTotalPages() {
        return (int) Math.ceil((double) getTotal() / (double) getSize());
    }

    /**
     * 获取内容
     *
     * @return 内容
     */
    public List<T> getContent() {
        return content;
    }

    /**
     * 获取总记录数
     *
     * @return 总记录数
     */
    public long getTotal() {
        return total;
    }

    /**
     * 获取分页信息
     *
     * @return 分页信息
     */
    public Pageable getPageable() {
        return pageable;
    }

    public Set<Long> getCategoryIdSet() {
        return categoryIdSet;
    }

    public void setCategoryIdSet(Set<Long> categoryIdSet) {
        this.categoryIdSet = categoryIdSet;
    }
}