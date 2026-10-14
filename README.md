# NOVA MOBILE — Quản lý cửa hàng điện thoại

Project Java Swing/Maven dành cho NetBeans, phát triển dựa trên các chức năng của project quản lý trà sữa được cung cấp. Bản giao diện 2 dùng nền kem, cam đất, điều hướng ngang và luồng quản lý theo thiết bị.

## Bố cục và nghiệp vụ mới

- Bàn điều hành: biểu đồ doanh thu 7 ngày, giao dịch gần đây, tồn kho theo hãng và các máy cần xử lý bảo hành.
- Lập đơn: phiếu tính tiền bên trái, bảng chọn máy bên phải; nhấp đúp máy để thêm nhanh vào đơn.
- Kho thiết bị: bảng toàn màn hình, bộ lọc sắp hết/hết hàng/ngừng bán và cửa sổ riêng để nhập hoặc chỉnh sửa máy.
- Sổ IMEI / Bảo hành: đăng ký từng máy đã bán theo IMEI, tính hạn từ ngày mua và cập nhật tình trạng dịch vụ.
- Nhân viên, tài khoản, thống kê, Gmail nằm trong nút **Quản trị** ở góc trên phải. Đổi mật khẩu và đăng xuất nằm trong nút tên tài khoản.

## Chạy ngay

1. Máy cần Java 17 trở lên. Có thể dùng Java đi kèm NetBeans.
2. Mở **ChayUngDung.cmd** trong thư mục project. File chạy có sẵn ở `dist/QuanLyDienThoai.jar`.
3. Đăng nhập Admin: **admin / admin123**. Nhân viên mẫu: **nhanvien01 / 123456**.
4. Database H2 tự tạo trong thư mục `data` bên cạnh file chạy. Dữ liệu của project điện thoại độc lập với project trà sữa.

Trong NetBeans: File → Open Project → chọn thư mục `QuanLyDienThoai` có `pom.xml` → Run Project. Để đóng gói lại: chạy `Build.ps1`, hoặc `mvn clean package`.

## Chức năng

- Đăng nhập, ghi nhớ đăng nhập, phân quyền Admin/nhân viên và đổi mật khẩu.
- Tổng quan doanh thu, hóa đơn hôm nay, số mẫu máy và số lượng tồn kho.
- Thêm/sửa/ngừng bán điện thoại; quản lý hãng, dung lượng, giá, màu sắc/bảo hành và số lượng tồn.
- Bán hàng: tìm kiếm không dấu, lọc hãng, giỏ hàng, sửa số lượng, giảm giá và thông tin khách hàng.
- Lưu hóa đơn và trừ tồn kho trong cùng một giao dịch; không cho bán vượt tồn. Đơn chuyển khoản đã lưu cũng giữ/trừ số máy đó.
- Thanh toán tiền mặt và VietQR; chuyển khoản cần nhân viên kiểm tra tiền về rồi xác nhận thủ công.
- Xem/lọc hóa đơn, xem chi tiết, xác nhận chuyển khoản, xuất PDF. Admin có thể xóa hóa đơn và hoàn số lượng về kho; thao tác này không thực hiện hoàn tiền qua ngân hàng.
- Quản lý nhân viên, tài khoản, khóa/mở tài khoản và đặt lại mật khẩu. Nhân viên chỉ thấy hóa đơn của mình.
- Thống kê theo khoảng ngày, doanh thu và sản phẩm bán chạy.
- Kích hoạt bảo hành từ dòng hóa đơn đã thanh toán: IMEI 15 số duy nhất, không đăng ký nhiều máy hơn số lượng đã bán, thời hạn 1–36 tháng tính từ ngày mua.
- Tra cứu theo IMEI/tên khách/số điện thoại, lọc còn hạn/hết hạn và cập nhật đang sửa/chờ trả/đã trả. Hóa đơn có sổ IMEI được giữ lại để bảo toàn nguồn bán.
- Khôi phục mật khẩu bằng OTP Gmail: 6 số, hạn 5 phút, mỗi mã dùng một lần, gửi lại sau 60 giây và khóa mã sau 5 lần nhập sai.
- Mật khẩu tài khoản được lưu bằng PBKDF2; mã OTP được lưu dưới dạng băm.

## Tài khoản nhận chuyển khoản

- Ngân hàng: **MB**
- Số tài khoản: **0787911669**
- Chủ tài khoản: **NGUYEN THI NGOC THUC**

QR được tải từ VietQR qua Internet. Nếu không tải được QR, hóa đơn vẫn được lưu; có thể chuyển khoản theo thông tin tài khoản rồi xác nhận tại mục Hóa đơn.

## Gmail OTP

Gmail gửi mã và email khôi phục mặc định của Admin: **ngthingocthuc@gmail.com**.

Trên máy hiện tại, cấu hình Gmail đã được lưu tại `data/gmail.properties` và mật khẩu ứng dụng được bảo vệ bằng Windows DPAPI tại `data/gmail-secret.dpapi`. Không có mật khẩu Gmail trong mã nguồn. Bản ZIP phân phối không chứa thư mục `data` hay mật khẩu ứng dụng.

Nếu giải nén ZIP vào thư mục khác hoặc chuyển sang máy khác:

1. Đăng nhập Admin → **Cấu hình Gmail**.
2. Nhập Gmail gửi mã và mật khẩu ứng dụng Gmail 16 chữ cái → **Lưu cấu hình gửi OTP**.
3. Kiểm tra email khôi phục của Admin → **Liên kết email khôi phục** nếu cần.
4. Đăng xuất → **Quên mật khẩu?** → nhập tên đăng nhập và email đã liên kết → gửi OTP → nhập mã và mật khẩu mới.

Email nhân viên được lấy từ email tài khoản hoặc email trong hồ sơ nhân viên. Nhân viên mẫu dùng email minh họa, cần sửa thành email thật trước khi dùng OTP. Gmail cần mật khẩu ứng dụng hợp lệ và kết nối Internet. Trên hệ điều hành khác, cấu hình `NOVA_SMTP_USER` và `NOVA_SMTP_PASSWORD` trong môi trường chạy.

## Kiểm tra

Đã build JAR thành công; 53 kiểm tra tích hợp bán hàng/tồn kho/phân quyền/PDF/OTP/bảo hành đã đạt, cùng kiểm tra dựng giao diện 11 màn hình. OTP được kiểm thử với bộ gửi mail giả lập, chưa kiểm thử gửi email thực tế qua Gmail. Database kiểm thử dùng H2 trong bộ nhớ vì môi trường kiểm thử hạn chế quyền kiểm tra ghi file trên Windows; cần chạy ứng dụng bình thường để kiểm tra lưu dữ liệu qua các lần mở lại.

Chạy lại kiểm tra bằng `checks/Check.ps1`. Các file kiểm thử và ảnh giao diện sinh ra trong `work`, không tác động database đang sử dụng.

Giá và số lượng máy mẫu là dữ liệu minh họa, có thể chỉnh trong mục Điện thoại & kho. Sao lưu thư mục `data` khi ứng dụng đã đóng để giữ dữ liệu.
