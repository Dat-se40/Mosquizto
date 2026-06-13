package com.example.mosquizto.Util;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.mosquizto.R;

public class AboutDialogHelper {

    // ─────────────────────────────────────────────
    //  1. CHÍNH SÁCH QUYỀN RIÊNG TƯ
    // ─────────────────────────────────────────────
    public static void showPrivacyPolicy(Context context) {
        String title = "Chính sách quyền riêng tư";

        String content = "<b>Cập nhật lần cuối: 01/06/2025</b><br/><br/>"

                + "<b>1. Thông tin chúng tôi thu thập</b><br/>"
                + "Mosquizto thu thập các thông tin sau khi bạn sử dụng ứng dụng:<br/>"
                + "• <b>Thông tin tài khoản:</b> Tên người dùng, địa chỉ email khi bạn đăng ký.<br/>"
                + "• <b>Nội dung học tập:</b> Các bộ thẻ (flashcard), câu hỏi, câu trả lời và ghi chú bạn tạo ra.<br/>"
                + "• <b>Dữ liệu học tập:</b> Tiến trình ôn tập, điểm số bài kiểm tra, thống kê học tập.<br/>"
                + "• <b>Thông tin thiết bị:</b> Hệ điều hành, phiên bản ứng dụng, nhật ký sự cố.<br/><br/>"

                + "<b>2. Cách chúng tôi sử dụng thông tin</b><br/>"
                + "Thông tin thu thập được sử dụng để:<br/>"
                + "• Cung cấp và cải thiện trải nghiệm học tập cá nhân hoá.<br/>"
                + "• Đồng bộ tiến trình học tập giữa các thiết bị của bạn.<br/>"
                + "• Gửi thông báo nhắc nhở học tập.<br/>"
                + "• Phân tích và khắc phục lỗi kỹ thuật.<br/><br/>"

                + "<b>3. Chia sẻ thông tin</b><br/>"
                + "Chúng tôi <b>không bán</b> thông tin cá nhân của bạn. Thông tin chỉ được chia sẻ khi:<br/>"
                + "• Bạn chủ động chia sẻ bộ thẻ công khai với cộng đồng.<br/>"
                + "• Yêu cầu pháp lý bắt buộc từ cơ quan có thẩm quyền.<br/><br/>"

                + "<b>4. Bảo mật dữ liệu</b><br/>"
                + "Mosquizto áp dụng các biện pháp bảo mật tiêu chuẩn ngành như mã hóa HTTPS, "
                + "lưu trữ mật khẩu bằng hashing để bảo vệ dữ liệu của bạn.<br/><br/>"

                + "<b>5. Quyền của bạn</b><br/>"
                + "Bạn có thể chỉnh sửa thông tin tài khoản hoặc xóa tài khoản bất kỳ lúc nào trong phần Cài đặt. "
                + "Khi xóa tài khoản, toàn bộ dữ liệu học tập của bạn sẽ bị xóa vĩnh viễn.<br/><br/>"

                + "<b>6. Liên hệ</b><br/>"
                + "Mọi thắc mắc về quyền riêng tư, vui lòng liên hệ:<br/>"
                + "📧 privacy@mosquizto.app";

        showScrollableDialog(context, title, content);
    }

    // ─────────────────────────────────────────────
    //  2. ĐIỀU KHOẢN DỊCH VỤ
    // ─────────────────────────────────────────────
    public static void showTermsOfService(Context context) {
        String title = "Điều khoản dịch vụ";

        String content = "<b>Cập nhật lần cuối: 01/06/2025</b><br/><br/>"
                + "Chào mừng bạn đến với <b>Mosquizto</b> — ứng dụng học flashcard thông minh. "
                + "Bằng việc sử dụng ứng dụng, bạn đồng ý với các điều khoản dưới đây.<br/><br/>"

                + "<b>1. Điều kiện sử dụng</b><br/>"
                + "• Bạn phải từ 13 tuổi trở lên để tạo tài khoản Mosquizto.<br/>"
                + "• Mỗi người dùng chỉ được phép sở hữu một tài khoản.<br/>"
                + "• Bạn chịu trách nhiệm bảo mật thông tin đăng nhập của mình.<br/><br/>"

                + "<b>2. Nội dung người dùng</b><br/>"
                + "• Bạn giữ toàn quyền sở hữu các bộ thẻ và nội dung học tập mình tạo ra.<br/>"
                + "• Khi chia sẻ nội dung công khai, bạn cấp cho Mosquizto quyền hiển thị nội dung "
                + "đó trong ứng dụng để phục vụ cộng đồng học tập.<br/>"
                + "• Nghiêm cấm đăng tải nội dung vi phạm bản quyền, nội dung độc hại hoặc không phù hợp.<br/><br/>"

                + "<b>3. Quy tắc cộng đồng</b><br/>"
                + "Mosquizto là không gian học tập. Người dùng <b>không được</b>:<br/>"
                + "• Sao chép, phân phối lại nội dung của người dùng khác khi chưa được phép.<br/>"
                + "• Sử dụng ứng dụng cho mục đích thương mại khi chưa có thỏa thuận riêng.<br/>"
                + "• Cố tình tấn công, làm gián đoạn hệ thống hoặc gây hại cho người dùng khác.<br/><br/>"

                + "<b>4. Tính năng và dịch vụ</b><br/>"
                + "• Mosquizto cung cấp tính năng tạo flashcard, kiểm tra (quiz), học offline và đồng bộ đám mây.<br/>"
                + "• Chúng tôi có thể thay đổi, tạm ngừng hoặc ngừng cung cấp một số tính năng mà không cần báo trước.<br/><br/>"

                + "<b>5. Chấm dứt tài khoản</b><br/>"
                + "Chúng tôi có quyền tạm khóa hoặc xóa tài khoản vi phạm điều khoản này. "
                + "Bạn cũng có thể tự xóa tài khoản bất kỳ lúc nào trong Cài đặt.<br/><br/>"

                + "<b>6. Liên hệ</b><br/>"
                + "📧 support@mosquizto.app";

        showScrollableDialog(context, title, content);
    }

    // ─────────────────────────────────────────────
    //  3. GIẤY PHÉP MÃ NGUỒN MỞ
    // ─────────────────────────────────────────────
    public static void showOpenSourceLicenses(Context context) {
        String title = "Giấy phép mã nguồn mở";

        String content = "Mosquizto được xây dựng với sự hỗ trợ của các thư viện mã nguồn mở sau. "
                + "Chúng tôi trân trọng và ghi nhận công sức của cộng đồng phát triển.<br/><br/>"

                + "<b>Dagger Hilt</b><br/>"
                + "Phiên bản: 2.48 | Giấy phép: Apache 2.0<br/>"
                + "Thư viện Dependency Injection giúp quản lý vòng đời và phụ thuộc trong ứng dụng Android.<br/>"
                + "<i>Copyright © Google LLC</i><br/><br/>"

                + "<b>Retrofit 2</b><br/>"
                + "Phiên bản: 2.9.0 | Giấy phép: Apache 2.0<br/>"
                + "Thư viện HTTP client dùng để giao tiếp với API backend của Mosquizto.<br/>"
                + "<i>Copyright © Square, Inc.</i><br/><br/>"

                + "<b>OkHttp</b><br/>"
                + "Phiên bản: 4.11.0 | Giấy phép: Apache 2.0<br/>"
                + "HTTP & HTTP/2 client cho Android, được sử dụng kết hợp với Retrofit.<br/>"
                + "<i>Copyright © Square, Inc.</i><br/><br/>"

                + "<b>Glide</b><br/>"
                + "Phiên bản: 4.16.0 | Giấy phép: BSD, MIT, Apache 2.0<br/>"
                + "Thư viện tải và hiển thị ảnh hiệu suất cao cho Android.<br/>"
                + "<i>Copyright © Google LLC</i><br/><br/>"

                + "<b>Material Components for Android</b><br/>"
                + "Phiên bản: 1.11.0 | Giấy phép: Apache 2.0<br/>"
                + "Bộ thành phần giao diện Material Design — Switch, Card, Button, v.v.<br/>"
                + "<i>Copyright © Google LLC</i><br/><br/>"

                + "<b>Room Persistence Library</b><br/>"
                + "Phiên bản: 2.6.1 | Giấy phép: Apache 2.0<br/>"
                + "Thư viện lưu trữ cục bộ (SQLite) dùng để lưu bộ thẻ offline.<br/>"
                + "<i>Copyright © Google LLC</i><br/><br/>"

                + "<b>Gson</b><br/>"
                + "Phiên bản: 2.10.1 | Giấy phép: Apache 2.0<br/>"
                + "Thư viện chuyển đổi JSON ↔ Java Object.<br/>"
                + "<i>Copyright © Google LLC</i><br/><br/>"

                + "<b>Lottie</b><br/>"
                + "Phiên bản: 6.1.0 | Giấy phép: Apache 2.0<br/>"
                + "Thư viện render animation từ file JSON cho giao diện ứng dụng.<br/>"
                + "<i>Copyright © Airbnb, Inc.</i><br/><br/>"

                + "─────────────────────<br/>"
                + "Toàn bộ văn bản giấy phép đầy đủ có tại:<br/>"
                + "https://opensource.org/licenses/Apache-2.0";

        showScrollableDialog(context, title, content);
    }

    // ─────────────────────────────────────────────
    //  4. TRUNG TÂM HỖ TRỢ
    // ─────────────────────────────────────────────
    public static void showSupportCenter(Context context) {
        String title = "Trung tâm hỗ trợ";

        String content = "<b>Chúng tôi luôn sẵn sàng hỗ trợ bạn!</b><br/><br/>"

                + "<b>🔹 Câu hỏi thường gặp</b><br/><br/>"

                + "<b>Tôi quên mật khẩu, phải làm sao?</b><br/>"
                + "Nhấn \"Quên mật khẩu\" ở màn hình đăng nhập. Chúng tôi sẽ gửi link đặt lại "
                + "mật khẩu qua email đã đăng ký của bạn.<br/><br/>"

                + "<b>Làm sao để học offline?</b><br/>"
                + "Bật công tắc <i>Lưu offline</i> trong Cài đặt → Học offline. Bộ thẻ của bạn "
                + "sẽ được tải xuống và có thể học ngay cả khi không có mạng.<br/><br/>"

                + "<b>Dữ liệu học tập có đồng bộ giữa các thiết bị không?</b><br/>"
                + "Có! Tiến trình học và bộ thẻ của bạn tự động đồng bộ qua tài khoản Mosquizto "
                + "khi có kết nối internet.<br/><br/>"

                + "<b>Tôi muốn chia sẻ bộ thẻ với bạn bè?</b><br/>"
                + "Mở bộ thẻ → nhấn biểu tượng Chia sẻ → chọn \"Chia sẻ link\" hoặc \"Đặt công khai\" "
                + "để cộng đồng cùng học.<br/><br/>"

                + "<b>Làm sao để xóa tài khoản?</b><br/>"
                + "Vào Cài đặt → kéo xuống cuối → nhấn <i>Xóa tài khoản</i>. "
                + "Hành động này không thể hoàn tác.<br/><br/>"

                + "─────────────────────<br/><br/>"

                + "<b>📬 Liên hệ trực tiếp</b><br/>"
                + "Không tìm được câu trả lời? Đội ngũ hỗ trợ Mosquizto sẵn sàng giúp bạn:<br/><br/>"
                + "• <b>Email:</b> support@mosquizto.app<br/>"
                + "• <b>Thời gian phản hồi:</b> 24–48 giờ làm việc<br/>"
                + "• <b>Ngôn ngữ hỗ trợ:</b> Tiếng Việt &amp; English<br/><br/>"

                + "Phiên bản ứng dụng: <b>1.0.0</b><br/>"
                + "© 2025 Mosquizto Team. All rights reserved.";

        showScrollableDialog(context, title, content);
    }

    // ─────────────────────────────────────────────
    //  INTERNAL: Dialog chung có ScrollView + nút Đóng
    // ─────────────────────────────────────────────
    private static void showScrollableDialog(Context context, String title, String htmlContent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AboutDialogTheme);

        // Inflate custom view
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_about_content, null);

        TextView tvTitle   = dialogView.findViewById(R.id.dialogTitle);
        TextView tvContent = dialogView.findViewById(R.id.dialogContent);

        tvTitle.setText(title);
        tvContent.setText(Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY));

        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Nút X ở header
        ImageButton btnClose = dialogView.findViewById(R.id.btnCloseDialog);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        // Nút "Đóng" ở footer
        TextView btnDismiss = dialogView.findViewById(R.id.btnDismissDialog);
        if (btnDismiss != null) {
            btnDismiss.setOnClickListener(v -> dialog.dismiss());
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.show();
    }
}