/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rename;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 * @author tungi
 */
public class Rename {

   public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // Nhận đường dẫn từ người dùng
        System.out.print("Nhập đường dẫn thư mục: ");
        String directoryPath = scanner.nextLine();
        scanner.close();
        
        try {
            // Chuyển đổi đường dẫn thành đối tượng Path
            Path directory = Paths.get(directoryPath);
            
            // Kiểm tra xem đường dẫn có tồn tại và là thư mục không
            if (!Files.exists(directory) || !Files.isDirectory(directory)) {
                System.out.println("Đường dẫn không hợp lệ hoặc không phải là thư mục!");
                return;
            }
            
            // Lấy danh sách các file trong thư mục
            File[] files = directory.toFile().listFiles();
            
            if (files == null || files.length == 0) {
                System.out.println("Thư mục trống hoặc không thể đọc được nội dung!");
                return;
            }
            
            int count = 0;
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    
                    // Kiểm tra xem tên file có chứa "_SE" không
                    int indexOfSE = fileName.indexOf("_SE");
                    if (indexOfSE >= 0) {
                        // Tạo tên file mới bằng cách loại bỏ nội dung phía trước "_SE"
                        String newFileName = fileName.substring(indexOfSE);
                        
                        // Tạo đường dẫn mới
                        File newFile = new File(file.getParent(), newFileName);
                        
                        // Đổi tên file
                        boolean success = file.renameTo(newFile);
                        if (success) {
                            System.out.println("Đổi tên thành công: " + fileName + " -> " + newFileName);
                            count++;
                        } else {
                            System.out.println("Không thể đổi tên file: " + fileName);
                        }
                    }
                }
            }
            
            System.out.println("Hoàn thành! Đã đổi tên " + count + " file.");
            
        } catch (Exception e) {
            System.out.println("Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
