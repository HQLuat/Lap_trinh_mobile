package vn.edu.hcmuaf.fit.travelapp.auth.data.model;

public class Event<T> {

    private final T content;
    private boolean hasBeenHandled = false;

    public Event(T content) {
        this.content = content;
    }

    /**
     * Trả về nội dung nếu sự kiện chưa được xử lý, và đánh dấu là đã xử lý.
     * Nếu đã xử lý rồi, trả về null.
     */
    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    /**
     * Trả về nội dung ngay cả khi đã xử lý rồi.
     */
    public T peekContent() {
        return content;
    }
}
