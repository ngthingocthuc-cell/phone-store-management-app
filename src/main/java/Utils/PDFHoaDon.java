package Utils;

import DAO.HoaDonDAO.HoaDonItem;
import DAO.HoaDonDAO.ChiTietItem;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public final class PDFHoaDon {

    private PDFHoaDon() {
    }

    // =====================================================
    // XUẤT HÓA ĐƠN PDF
    // =====================================================

    public static void xuatHoaDon(
            File file,
            HoaDonItem hoaDon,
            List<ChiTietItem> chiTiet
    ) throws Exception {

        if (file == null || hoaDon == null) {
            throw new IllegalArgumentException(
                    "Thiếu thông tin để xuất hóa đơn."
            );
        }

        if (chiTiet == null) {
            throw new IllegalArgumentException(
                    "Danh sách chi tiết hóa đơn không hợp lệ."
            );
        }

        File parent = file.getAbsoluteFile().getParentFile();

        if (parent != null) {
            Files.createDirectories(parent.toPath());
        }

        BaseFont baseFont = taoFont();

        com.lowagie.text.Font titleFont =
                new com.lowagie.text.Font(
                        baseFont,
                        20,
                        com.lowagie.text.Font.BOLD,
                        new Color(0, 137, 123)
                );

        com.lowagie.text.Font subtitleFont =
                new com.lowagie.text.Font(
                        baseFont,
                        10,
                        com.lowagie.text.Font.NORMAL,
                        Color.GRAY
                );

        com.lowagie.text.Font normalFont =
                new com.lowagie.text.Font(
                        baseFont,
                        10,
                        com.lowagie.text.Font.NORMAL,
                        Color.BLACK
                );

        com.lowagie.text.Font boldFont =
                new com.lowagie.text.Font(
                        baseFont,
                        10,
                        com.lowagie.text.Font.BOLD,
                        Color.BLACK
                );

        com.lowagie.text.Font tableHeaderFont =
                new com.lowagie.text.Font(
                        baseFont,
                        9,
                        com.lowagie.text.Font.BOLD,
                        Color.WHITE
                );

        Document document = new Document(
                PageSize.A4,
                32,
                32,
                35,
                35
        );

        try {

            PdfWriter.getInstance(
                    document,
                    new FileOutputStream(file)
            );

            document.open();

            // ---------------------------------------------
            // TIÊU ĐỀ
            // ---------------------------------------------

            Paragraph brand = new Paragraph(
                    "NOVA MOBILE",
                    titleFont
            );

            brand.setAlignment(Element.ALIGN_CENTER);

            brand.setSpacingAfter(4);

            document.add(brand);

            Paragraph address = new Paragraph(
                    "HÓA ĐƠN THANH TOÁN",
                    boldFont
            );

            address.setAlignment(Element.ALIGN_CENTER);

            address.setSpacingAfter(4);

            document.add(address);

            Paragraph line = new Paragraph(
                    "Cảm ơn quý khách đã ủng hộ!",
                    subtitleFont
            );

            line.setAlignment(Element.ALIGN_CENTER);

            line.setSpacingAfter(18);

            document.add(line);

            // ---------------------------------------------
            // THÔNG TIN HÓA ĐƠN
            // ---------------------------------------------

            PdfPTable info = new PdfPTable(
                    new float[]{1.2f, 2.1f, 1.2f, 2.1f}
            );

            info.setWidthPercentage(100);

            info.setSpacingAfter(15);

            String maHD = String.format(
                    Locale.ROOT,
                    "HD%05d",
                    hoaDon.getMaHD()
            );

            String ngayLap = hoaDon.getNgayLap() == null
                    ? ""
                    : new SimpleDateFormat(
                            "dd/MM/yyyy HH:mm:ss"
                    ).format(hoaDon.getNgayLap());

            addInfoCell(info, "Mã hóa đơn:", boldFont);
            addInfoCell(info, maHD, normalFont);

            addInfoCell(info, "Ngày lập:", boldFont);
            addInfoCell(info, ngayLap, normalFont);

            addInfoCell(info, "Nhân viên:", boldFont);
            addInfoCell(
                    info,
                    hoaDon.getTenNhanVien(),
                    normalFont
            );

            addInfoCell(info, "Khách hàng:", boldFont);

            String customer = hoaDon.getTenKhachHang();

            if (customer == null || customer.isBlank()) {
                customer = "Khách lẻ";
            }

            addInfoCell(info, customer, normalFont);

            addInfoCell(info, "Thanh toán:", boldFont);
            addInfoCell(
                    info,
                    hoaDon.getPhuongThucTT(),
                    normalFont
            );

            addInfoCell(info, "Trạng thái:", boldFont);
            addInfoCell(
                    info,
                    hoaDon.getTrangThai(),
                    normalFont
            );

            document.add(info);

            // ---------------------------------------------
            // CHI TIẾT MÓN
            // ---------------------------------------------

            Paragraph detailTitle = new Paragraph(
                    "CHI TIẾT ĐƠN HÀNG",
                    boldFont
            );

            detailTitle.setSpacingAfter(8);

            document.add(detailTitle);

            PdfPTable table = new PdfPTable(
                    new float[]{
                            2.0f,
                            0.55f,
                            2.0f,
                            1.05f,
                            0.55f,
                            1.2f
                    }
            );

            table.setWidthPercentage(100);

            table.setHeaderRows(1);

            table.addCell(
                    createCell(
                            "Sản phẩm",
                            tableHeaderFont,
                            Element.ALIGN_LEFT,
                            true
                    )
            );

            table.addCell(
                    createCell(
                            "Dung lượng",
                            tableHeaderFont,
                            Element.ALIGN_CENTER,
                            true
                    )
            );

            table.addCell(
                    createCell(
                            "PhuKien",
                            tableHeaderFont,
                            Element.ALIGN_LEFT,
                            true
                    )
            );

            table.addCell(
                    createCell(
                            "Đơn giá",
                            tableHeaderFont,
                            Element.ALIGN_RIGHT,
                            true
                    )
            );

            table.addCell(
                    createCell(
                            "SL",
                            tableHeaderFont,
                            Element.ALIGN_CENTER,
                            true
                    )
            );

            table.addCell(
                    createCell(
                            "Thành tiền",
                            tableHeaderFont,
                            Element.ALIGN_RIGHT,
                            true
                    )
            );

            NumberFormat currency =
                    NumberFormat.getNumberInstance(
                            Locale.forLanguageTag("vi-VN")
                    );

            for (ChiTietItem item : chiTiet) {

                table.addCell(
                        createCell(
                                item.getTenSanPham(),
                                normalFont,
                                Element.ALIGN_LEFT,
                                false
                        )
                );

                table.addCell(
                        createCell(
                                item.getDungLuong(),
                                normalFont,
                                Element.ALIGN_CENTER,
                                false
                        )
                );

                table.addCell(
                        createCell(
                                item.getTenPhuKien(),
                                normalFont,
                                Element.ALIGN_LEFT,
                                false
                        )
                );

                table.addCell(
                        createCell(
                                currency.format(item.getDonGia()),
                                normalFont,
                                Element.ALIGN_RIGHT,
                                false
                        )
                );

                table.addCell(
                        createCell(
                                String.valueOf(item.getSoLuong()),
                                normalFont,
                                Element.ALIGN_CENTER,
                                false
                        )
                );

                table.addCell(
                        createCell(
                                currency.format(item.getThanhTien()),
                                normalFont,
                                Element.ALIGN_RIGHT,
                                false
                        )
                );
            }

            document.add(table);

            // ---------------------------------------------
            // TỔNG TIỀN
            // ---------------------------------------------

            PdfPTable totals = new PdfPTable(
                    new float[]{2.0f, 1.0f}
            );

            totals.setHorizontalAlignment(
                    Element.ALIGN_RIGHT
            );

            totals.setWidthPercentage(45);

            totals.setSpacingBefore(14);

            totals.addCell(
                    createCell(
                            "Tổng tiền:",
                            normalFont,
                            Element.ALIGN_RIGHT,
                            false
                    )
            );

            totals.addCell(
                    createCell(
                            currency.format(hoaDon.getTongTien())
                                    + " đ",
                            normalFont,
                            Element.ALIGN_RIGHT,
                            false
                    )
            );

            totals.addCell(
                    createCell(
                            "Giảm giá:",
                            normalFont,
                            Element.ALIGN_RIGHT,
                            false
                    )
            );

            totals.addCell(
                    createCell(
                            currency.format(hoaDon.getGiamGia())
                                    + " đ",
                            normalFont,
                            Element.ALIGN_RIGHT,
                            false
                    )
            );

            totals.addCell(
                    createCell(
                            "THÀNH TIỀN:",
                            boldFont,
                            Element.ALIGN_RIGHT,
                            false
                    )
            );

            totals.addCell(
                    createCell(
                            currency.format(hoaDon.getThanhTien())
                                    + " đ",
                            boldFont,
                            Element.ALIGN_RIGHT,
                            false
                    )
            );

            document.add(totals);

            // ---------------------------------------------
            // CHÂN TRANG
            // ---------------------------------------------

            Paragraph footer = new Paragraph(
                    "\n\nHóa đơn được xuất từ phần mềm quản lý quán điện thoại.",
                    subtitleFont
            );

            footer.setAlignment(Element.ALIGN_CENTER);

            footer.setSpacingBefore(12);

            document.add(footer);

        } finally {

            if (document.isOpen()) {
                document.close();
            }
        }
    }

    // =====================================================
    // CHỌN FONT HỖ TRỢ TIẾNG VIỆT TRÊN WINDOWS
    // =====================================================

    private static BaseFont taoFont() throws Exception {

        String[] paths = {
                "C:/Windows/Fonts/arial.ttf",
                "C:/Windows/Fonts/tahoma.ttf",
                "C:/Windows/Fonts/segoeui.ttf"
        };

        for (String path : paths) {

            if (Files.exists(new File(path).toPath())) {

                return BaseFont.createFont(
                        path,
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED
                );
            }
        }

        throw new IllegalStateException(
                "Không tìm thấy font Unicode trên Windows. "
                + "Không thể đảm bảo tiếng Việt hiển thị đúng trong PDF."
        );
    }

    // =====================================================
    // TẠO Ô BẢNG
    // =====================================================

    private static PdfPCell createCell(
            String text,
            com.lowagie.text.Font font,
            int alignment,
            boolean header
    ) {

        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text == null ? "" : text,
                        font
                )
        );

        cell.setPadding(6);

        cell.setHorizontalAlignment(alignment);

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        cell.setBorderColor(
                new Color(225, 228, 237)
        );

        if (header) {

            cell.setBackgroundColor(
                    new Color(0, 137, 123)
            );
        }

        return cell;
    }

    private static void addInfoCell(
            PdfPTable table,
            String text,
            com.lowagie.text.Font font
    ) {

        PdfPCell cell = new PdfPCell(
                new Phrase(
                        text == null ? "" : text,
                        font
                )
        );

        cell.setBorderColor(
                new Color(235, 237, 242)
        );

        cell.setPadding(6);

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        table.addCell(cell);
    }
}