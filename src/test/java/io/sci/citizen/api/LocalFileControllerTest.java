package io.sci.citizen.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalFileControllerTest {

    @TempDir
    Path tempDir;

    @Test
    void getImageReturnsFileContentWhenFileExists() throws IOException {
        LocalFileController controller = new LocalFileController();
        String location = tempDir.toString() + "/";
        ReflectionTestUtils.setField(controller, "location", location);

        String fileName = "sample.txt";
        byte[] expectedContent = "hello world".getBytes();
        Path filePath = Path.of(location + "\\" + fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, expectedContent);

        ResponseEntity<byte[]> response = controller.getImage(fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().containsExactly(expectedContent);
    }

    @Test
    void getImageReturnsBadRequestWhenFileMissing() {
        LocalFileController controller = new LocalFileController();
        String location = tempDir.toString() + "/";
        ReflectionTestUtils.setField(controller, "location", location);

        ResponseEntity<byte[]> response = controller.getImage("missing.png");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void getFileReturnsContentFromSubfolderWhenFileExists() throws IOException {
        LocalFileController controller = new LocalFileController();
        String location = tempDir.toString() + "/";
        ReflectionTestUtils.setField(controller, "location", location);

        String subfolder = "images";
        String fileName = "picture.jpg";
        byte[] expectedContent = {1, 2, 3, 4};

        Path folderPath = tempDir.resolve(subfolder);
        Files.createDirectories(folderPath);
        Path filePath = Path.of(location + subfolder + "/" + fileName);
        Files.write(filePath, expectedContent);

        ResponseEntity<byte[]> response = controller.getFile(subfolder, fileName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().containsExactly(expectedContent);
    }

    @Test
    void getFileReturnsBadRequestWhenFileMissing() {
        LocalFileController controller = new LocalFileController();
        String location = tempDir.toString() + "/";
        ReflectionTestUtils.setField(controller, "location", location);

        ResponseEntity<byte[]> response = controller.getFile("images", "unknown.jpg");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }
}
