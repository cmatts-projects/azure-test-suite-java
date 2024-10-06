package co.uk.cmatts.azure.blobstorage;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.io.InputStream;
import java.nio.file.Path;

public class StorageContainerService {

    private static final String AZURE_STORAGE_CONNECTION_STRING = "AZURE_STORAGE_CONNECTION_STRING";
    private BlobContainerClient containerClient;
    private String containerName;

    public StorageContainerService(String containerName) {
        this.containerName = containerName;
    }

    private BlobContainerClient getContainerClient() {
        if (containerClient != null) {
            return containerClient;
        }

        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                .connectionString(System.getenv(AZURE_STORAGE_CONNECTION_STRING))
                .buildClient();
        containerClient = serviceClient.getBlobContainerClient(containerName);

        return containerClient;
    }

    public boolean containerExists() {
        return getContainerClient().exists();
    }

    public void createContainer() {
        getContainerClient().create();
    }

    public void writeToContainer(String filename, Path path) {
        BlobClient blobClient = getContainerClient().getBlobClient(filename);
        blobClient.uploadFromFile(path.toString());
    }

    public void writeToContainer(String filename, InputStream inputStream) {
        BlobClient blobClient = getContainerClient().getBlobClient(filename);
        blobClient.upload(inputStream);
    }

    public void writeToContainer(String filename, String content) {
        BlobClient blobClient = getContainerClient().getBlobClient(filename);
        blobClient.upload(BinaryData.fromString(content));
    }

    public boolean fileExists(String filename) {
        BlobClient blobClient = getContainerClient().getBlobClient(filename);
        return blobClient.exists();
    }

    public InputStream readFromContainer(String filename) {
        BlobClient blobClient = getContainerClient().getBlobClient(filename);
        return blobClient.downloadContent().toStream();
    }
}
