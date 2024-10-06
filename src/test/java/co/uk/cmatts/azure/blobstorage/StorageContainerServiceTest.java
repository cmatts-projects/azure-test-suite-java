package co.uk.cmatts.azure.blobstorage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@ExtendWith(SystemStubsExtension.class)
class StorageContainerServiceTest {
    private static final String TEST_ACCOUNT = "devstoreaccount1";
    private static final String TEST_ACCOUNT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";
    private static final String TEST_CONTAINER_NAME = "mycontainer";

    private static final String TEST_CONTENT = "{ \"content\": \"some content\" }";
    private static final String TEST_RESOURCES_FILE = "/my/test/resource.txt";
    private static final String TEST_RESOURCE_NAME = "MyFile.txt";

    @SystemStub
    private static EnvironmentVariables environmentVariables;

    @Container
    private static final GenericContainer<?> LOCAL_AZURITE_CONTAINER = new GenericContainer<>("mcr.microsoft.com/azure-storage/azurite")
            .withExposedPorts(10000);

    private static final StorageContainerService storageContainerService = new StorageContainerService(TEST_CONTAINER_NAME);

    @BeforeAll
    static void beforeAll() {
        String storageEndpoint = String.format("http://%s:%d/%s",
                LOCAL_AZURITE_CONTAINER.getHost(),
                LOCAL_AZURITE_CONTAINER.getMappedPort(10000),
                TEST_ACCOUNT);

        environmentVariables
                .set("AZURE_STORAGE_CONNECTION_STRING", String.format("DefaultEndpointsProtocol=http;AccountName=%s;AccountKey=%s;BlobEndpoint=%s;",
                        TEST_ACCOUNT, TEST_ACCOUNT_KEY, storageEndpoint));

        assertThat(storageContainerService.containerExists()).isFalse();
        storageContainerService.createContainer();
    }

    @Test
    void containerShouldExist() {
        assertThat(storageContainerService.containerExists()).isTrue();
    }

    @Test
    void shouldWriteFileToContainer() throws Exception {
        String filename = TEST_RESOURCES_FILE + "_from_file";
        assertThat(storageContainerService.fileExists(filename)).isFalse();

        Path localFile = Paths.get(this.getClass().getClassLoader().getResource(TEST_RESOURCE_NAME).toURI());
        storageContainerService.writeToContainer(filename, localFile);

        assertThat(storageContainerService.fileExists(filename)).isTrue();
    }

    @Test
    void shouldWriteInputStreamToContainer() throws Exception {
        String filename = TEST_RESOURCES_FILE + "_from_stream";
        assertThat(storageContainerService.fileExists(filename)).isFalse();

        File localFile = new File(this.getClass().getClassLoader().getResource(TEST_RESOURCE_NAME).toURI());
        try (FileInputStream inputStream = new FileInputStream(localFile)) {
            storageContainerService.writeToContainer(filename, inputStream);
        }

        assertThat(storageContainerService.fileExists(filename)).isTrue();
    }

    @Test
    void shouldWriteStringToContainer() {
        String filename = TEST_RESOURCES_FILE + "_from_string";
        assertThat(storageContainerService.fileExists(filename)).isFalse();

        storageContainerService.writeToContainer(filename, TEST_CONTENT);

        assertThat(storageContainerService.fileExists(filename)).isTrue();
    }

    @Test
    void shouldReadFromContainer() throws Exception {
        String filename = TEST_RESOURCES_FILE;
        storageContainerService.writeToContainer(filename, TEST_CONTENT);

        try (InputStream StorageInputStream = storageContainerService.readFromContainer(filename)) {
            String actualFileContent = new String(StorageInputStream.readAllBytes(), UTF_8);
            assertThat(actualFileContent).isEqualTo(TEST_CONTENT);
        }
    }
}