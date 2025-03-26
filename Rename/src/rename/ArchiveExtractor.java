import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.Scanner;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class ArchiveExtractor {
    
    // Danh sách định dạng tập tin nén cần kiểm tra đệ quy
    private static final Set<String> ARCHIVE_EXTENSIONS = new HashSet<>(
            Arrays.asList(".zip", ".rar"));
    
    /**
     * Kiểm tra xem tập tin có phải là tập tin nén không
     */
    private static boolean isArchiveFile(String fileName) {
        fileName = fileName.toLowerCase();
        for (String ext : ARCHIVE_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Xử lý thư mục và tìm tất cả các tập tin nén trong đó (đệ quy)
     */
    private static void processDirectory(File directory) throws Exception {
        System.out.println("Đang quét thư mục: " + directory.getPath());
        File[] files = directory.listFiles();
        
        if (files == null || files.length == 0) {
            System.out.println("Thư mục trống: " + directory.getPath());
            return;
        }
        
        int archiveCount = 0;
        
        for (File file : files) {
            if (file.isDirectory()) {
                // Đệ quy vào thư mục con
                processDirectory(file);
            } else if (isArchiveFile(file.getName())) {
                // Đây là tập tin nén, tạo thư mục đích bên cạnh tập tin nén
                String subDirName = file.getName().replaceAll("[.][^.]+$", "") + "_extracted";
                File extractDir = new File(file.getParentFile(), subDirName);
                
                if (!extractDir.exists()) {
                    extractDir.mkdirs();
                }
                
                System.out.println("Đang giải nén: " + file.getPath() + " vào " + extractDir.getPath());
                extractArchive(file, extractDir);
                archiveCount++;
            }
        }
        
        System.out.println("Đã tìm thấy và giải nén " + archiveCount + " tập tin nén trong thư mục: " + directory.getPath());
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== CHƯƠNG TRÌNH GIẢI NÉN TẬP TIN ===");
        
        // Nhập đường dẫn tập tin/thư mục nguồn
        System.out.print("Nhập đường dẫn đến tập tin/thư mục chứa tập tin nén: ");
        String sourcePath = scanner.nextLine();
        
        try {
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()) {
                System.err.println("Lỗi: Đường dẫn không tồn tại: " + sourcePath);
                return;
            }
            
            System.out.println("Bắt đầu quá trình...");
            
            // Xử lý tùy thuộc vào nguồn là thư mục hay tập tin
            if (sourceFile.isDirectory()) {
                System.out.println("Đang quét thư mục nguồn để tìm tập tin nén...");
                processDirectory(sourceFile);
            } else {
                // Trường hợp là tập tin
                if (isArchiveFile(sourceFile.getName())) {
                    System.out.println("Đang giải nén tập tin: " + sourceFile.getName());
                    File extractDir = new File(sourceFile.getParent(), 
                                             sourceFile.getName().replaceAll("[.][^.]+$", "") + "_extracted");
                    extractArchive(sourceFile, extractDir);
                } else {
                    System.out.println("Tập tin không phải định dạng nén được hỗ trợ: " + sourceFile.getName());
                }
            }
            
            System.out.println("Đã hoàn thành!");
            
        } catch (Exception e) {
            System.err.println("Lỗi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
    
    /**
     * Giải nén tập tin và đệ quy với các tập tin nén bên trong
     */
    public static void extractArchive(File archiveFile, File destDir) throws Exception {
        String fileName = archiveFile.getName().toLowerCase();
        
        if (fileName.endsWith(".zip")) {
            extractZip(archiveFile, destDir);
        } else if (fileName.endsWith(".rar")) {
            extractRar(archiveFile, destDir);
        } else if (fileName.endsWith(".7z")) {
            extract7Zip(archiveFile, destDir);
        } else if (fileName.endsWith(".tar") || fileName.endsWith(".tar.gz") || 
                  fileName.endsWith(".tgz") || fileName.endsWith(".tar.bz2") || fileName.endsWith(".gz") || fileName.endsWith(".bz2")) {
            extractTar(archiveFile, destDir);
        } else {
            System.out.println("Định dạng không được hỗ trợ: " + fileName);
        }
    }
    
    /**
     * Giải nén tập tin ZIP và đệ quy các tập tin nén bên trong
     */
    private static void extractZip(File zipFile, File destDir) throws Exception {
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryFile = new File(destDir, entry.getName());
                
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                    continue;
                }
                
                // Đảm bảo thư mục cha tồn tại
                File parent = entryFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                
                // Giải nén tập tin
                try (InputStream in = zip.getInputStream(entry);
                     FileOutputStream out = new FileOutputStream(entryFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                
                System.out.println("Đã giải nén: " + entryFile.getPath());
                
                // Kiểm tra và giải nén đệ quy nếu là tập tin nén
                checkAndExtractRecursively(entryFile);
            }
        }
    }
    
    /**
     * Giải nén tập tin RAR và đệ quy các tập tin nén bên trong
     */
    private static void extractRar(File rarFile, File destDir) throws Exception {
        try {
            // Khởi tạo 7-Zip JBinding
            SevenZip.initSevenZipFromPlatformJAR();
            
            RandomAccessFile randomAccessFile = new RandomAccessFile(rarFile, "r");
            RandomAccessFileInStream inStream = new RandomAccessFileInStream(randomAccessFile);
            
            IInArchive archive = SevenZip.openInArchive(ArchiveFormat.RAR, inStream);
            ISimpleInArchive simpleInArchive = archive.getSimpleInterface();
            
            for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                if (!item.isFolder()) {
                    String path = item.getPath();
                    File outFile = new File(destDir, path);
                    
                    // Đảm bảo thư mục cha tồn tại
                    File parent = outFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    
                    // Giải nén tập tin
                    ExtractOperationResult result = item.extractSlow(data -> {
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            fos.write(data);
                            return data.length;
                        } catch (Exception e) {
                            return -1;
                        }
                    });
                    
                    if (result == ExtractOperationResult.OK) {
                        System.out.println("Đã giải nén: " + outFile.getPath());
                        
                        // Kiểm tra và giải nén đệ quy nếu là tập tin nén
                        checkAndExtractRecursively(outFile);
                    } else {
                        System.err.println("Lỗi khi giải nén: " + path + " - " + result);
                    }
                }
            }
            
            archive.close();
            inStream.close();
            randomAccessFile.close();
            
        } catch (Exception e) {
            System.err.println("Lỗi khi giải nén RAR: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Giải nén tập tin 7Z và đệ quy các tập tin nén bên trong
     */
    private static void extract7Zip(File sevenZipFile, File destDir) throws Exception {
        try {
            // Khởi tạo 7-Zip JBinding
            SevenZip.initSevenZipFromPlatformJAR();
            
            RandomAccessFile randomAccessFile = new RandomAccessFile(sevenZipFile, "r");
            RandomAccessFileInStream inStream = new RandomAccessFileInStream(randomAccessFile);
            
            IInArchive archive = SevenZip.openInArchive(ArchiveFormat.SEVEN_ZIP, inStream);
            ISimpleInArchive simpleInArchive = archive.getSimpleInterface();
            
            for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                if (!item.isFolder()) {
                    String path = item.getPath();
                    File outFile = new File(destDir, path);
                    
                    // Đảm bảo thư mục cha tồn tại
                    File parent = outFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    
                    // Giải nén tập tin
                    ExtractOperationResult result = item.extractSlow(data -> {
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            fos.write(data);
                            return data.length;
                        } catch (Exception e) {
                            return -1;
                        }
                    });
                    
                    if (result == ExtractOperationResult.OK) {
                        System.out.println("Đã giải nén: " + outFile.getPath());
                        
                        // Kiểm tra và giải nén đệ quy nếu là tập tin nén
                        checkAndExtractRecursively(outFile);
                    } else {
                        System.err.println("Lỗi khi giải nén: " + path + " - " + result);
                    }
                }
            }
            
            archive.close();
            inStream.close();
            randomAccessFile.close();
            
        } catch (Exception e) {
            System.err.println("Lỗi khi giải nén 7Z: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Giải nén tập tin TAR và đệ quy các tập tin nén bên trong
     */
    private static void extractTar(File tarFile, File destDir) throws Exception {
        InputStream inputStream = new FileInputStream(tarFile);
        
        // Xử lý gzip hoặc bzip2 nếu cần
        if (tarFile.getName().endsWith(".gz") || tarFile.getName().endsWith(".tgz")) {
            inputStream = new CompressorStreamFactory()
                    .createCompressorInputStream(CompressorStreamFactory.GZIP, inputStream);
        } else if (tarFile.getName().endsWith(".bz2")) {
            inputStream = new CompressorStreamFactory()
                    .createCompressorInputStream(CompressorStreamFactory.BZIP2, inputStream);
        }
        
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(inputStream)) {
            ArchiveEntry entry;
            
            while ((entry = tarIn.getNextEntry()) != null) {
                File entryFile = new File(destDir, entry.getName());
                
                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                    continue;
                }
                
                // Đảm bảo thư mục cha tồn tại
                File parent = entryFile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                
                // Giải nén tập tin
                try (FileOutputStream out = new FileOutputStream(entryFile)) {
                    IOUtils.copy(tarIn, out);
                }
                
                System.out.println("Đã giải nén: " + entryFile.getPath());
                
                // Kiểm tra và giải nén đệ quy nếu là tập tin nén
                checkAndExtractRecursively(entryFile);
            }
        }
    }
    
    /**
     * Kiểm tra nếu tập tin là một tập tin nén và giải nén đệ quy
     */
    private static void checkAndExtractRecursively(File file) throws Exception {
        if (!file.isFile()) {
            return;
        }
        
        String fileName = file.getName().toLowerCase();
        boolean isArchive = false;
        
        for (String ext : ARCHIVE_EXTENSIONS) {
            if (fileName.endsWith(ext)) {
                isArchive = true;
                break;
            }
        }
        
        if (isArchive) {
            System.out.println("Phát hiện tập tin nén bên trong: " + file.getPath());
            File extractDir = new File(file.getParentFile(), fileName + "_extracted");
            extractDir.mkdirs();
            
            extractArchive(file, extractDir);
        }
    }
}