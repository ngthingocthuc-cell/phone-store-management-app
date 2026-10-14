# Luồng nghiệp vụ NOVA

## Ba nhóm công việc

1. **Trước bán:** quản trị viên nhập mẫu điện thoại theo hãng và dung lượng, đặt giá bán và số lượng tồn. Các bộ lọc tách mẫu sắp hết, hết hàng và ngừng kinh doanh.
2. **Khi bán:** nhân viên chọn thiết bị vào phiếu tính tiền, sửa số lượng và giảm giá. Lưu hóa đơn và trừ kho cùng một giao dịch SQL; một dòng thiếu kho sẽ khiến toàn bộ giao dịch hoàn tác. Chuyển khoản chờ nhân viên xác nhận tiền đã về.
3. **Sau bán:** chọn máy trong hóa đơn đã thanh toán để kích hoạt bảo hành bằng IMEI. Sổ IMEI lưu khách hàng, số liên hệ, ngày mua, ngày hết hạn và nội dung xử lý dịch vụ.

## Quy tắc bảo hành

- Một IMEI chỉ xuất hiện một lần trong sổ đăng ký. Trong project này kiểm tra định dạng 15 chữ số; không kiểm tra mã với hãng hay cơ sở dữ liệu thiết bị bên ngoài.
- Số IMEI đăng ký cho mỗi dòng hóa đơn không được vượt số máy đã bán. DAO khóa dòng bán trong giao dịch khi kiểm tra số lượng và thêm đăng ký.
- Ngày hết hạn = ngày mua trên hóa đơn + số tháng bảo hành. Thời hạn cấu hình từ 1 đến 36 tháng.
- Còn hạn khi ngày hết hạn chưa qua ngày hiện tại. Tình trạng dịch vụ là thông tin riêng: Đang sử dụng → Đang sửa → Chờ trả khách → Đã trả khách. Có thể nhận máy hết hạn để theo dõi dịch vụ; project chưa tính phí sửa chữa.
- Đổi tình trạng dịch vụ cần ghi chú. Nhân viên đăng ký từ hóa đơn do mình phụ trách; tài khoản đã đăng nhập có thể tra cứu sổ và cập nhật dịch vụ tại cửa hàng.
- Hóa đơn đã có đăng ký IMEI không được xóa để giữ nguồn bán và ngày mua. Sổ bảo hành không đồng nghĩa với quản lý IMEI từng máy trong kho trước khi bán.

## Tính toán và hiển thị

- Tiền hàng = tổng (đơn giá × số lượng).
- Tiền cần thu = tiền hàng − giảm giá, với 0 ≤ giảm giá ≤ tiền hàng.
- Giá trị kho hiển thị = tổng (tồn kho × giá bán), không phải giá vốn hay lợi nhuận.
- Biểu đồ 7 ngày cộng các hóa đơn đã thanh toán theo ngày. Với nhân viên, dữ liệu doanh thu và hóa đơn thuộc nhân viên đang đăng nhập.

## Các lớp chính để đọc mã

- `View/MainView.java`: khung điều hướng ngang và các menu quản trị/tài khoản.
- `View/BanHangView.java` và `DAO/BanHangDAO.java`: phiếu tính tiền, kiểm tra tồn, giao dịch lưu bán hàng.
- `View/SanPhamView.java` và `DAO/DienThoaiDAO.java`: bảng kho và hộp thoại chỉnh sửa.
- `View/BaoHanhView.java` và `DAO/BaoHanhDAO.java`: sổ IMEI, kiểm tra số lượng đã bán và cập nhật dịch vụ.
- `View/DashboardView.java`: dữ liệu doanh thu, tồn theo hãng và biểu đồ Java2D.
- `src/main/resources/database/schema.sql`: cấu trúc H2, dữ liệu mẫu và bảng bảo hành mới.
- `checks/ProjectCheck.java`: các tình huống kiểm thử giao dịch, phân quyền, OTP và bảo hành.
