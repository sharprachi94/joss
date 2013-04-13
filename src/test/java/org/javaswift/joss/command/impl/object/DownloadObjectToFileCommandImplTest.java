package org.javaswift.joss.command.impl.object;

import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.javaswift.joss.command.impl.core.BaseCommandTest;
import org.javaswift.joss.exception.CommandException;
import org.javaswift.joss.instructions.DownloadInstructions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.javaswift.joss.headers.object.Etag.ETAG;
import static org.javaswift.joss.headers.object.ObjectContentLength.CONTENT_LENGTH;
import static org.javaswift.joss.headers.object.ObjectContentType.CONTENT_TYPE;
import static org.javaswift.joss.headers.object.ObjectLastModified.LAST_MODIFIED;
import static org.mockito.Mockito.when;

public class DownloadObjectToFileCommandImplTest extends BaseCommandTest {

    private File downloadedFile = new File("download.tmp");

    @Before
    public void setup() throws IOException {
        super.setup();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void teardown() {
        downloadedFile.delete();
    }

    private void prepareMetadata() throws IOException {
        InputStream inputStream = IOUtils.toInputStream("SOMEFILE");
        when(httpEntity.getContent()).thenReturn(inputStream);
        List<Header> headers = new ArrayList<Header>();
        prepareHeader(response, LAST_MODIFIED, "Mon, 03 Sep 2012 05:40:33 GMT");
        prepareHeader(response, ETAG, "e90629d47725dc3ad29e889d57e46106", headers);
        prepareHeader(response, CONTENT_LENGTH, "654321", headers);
        prepareHeader(response, CONTENT_TYPE, "image/png", headers);
        when(response.getAllHeaders()).thenReturn(headers.toArray(new Header[headers.size()]));
    }

    @Test
    public void testDownloadedFileExists() throws IOException {
        prepareMetadata();
        new DownloadObjectToFileCommandImpl(this.account, httpClient, defaultAccess, getObject("objectname"), new DownloadInstructions(), downloadedFile).call();
        assertTrue(downloadedFile.exists());
    }

    @Test(expected = CommandException.class)
    public void closeOutputStreamNull(@Mocked final FileOutputStream fos) throws Exception {
        prepareMetadata();
        new Expectations() {{
            new FileOutputStream(downloadedFile); result = new IOException();
        }};
        new DownloadObjectToFileCommandImpl(this.account, httpClient, defaultAccess,
                getObject("objectname"), new DownloadInstructions(), downloadedFile).call();
        new Verifications() {{
            fos.close(); times = 0;
        }};
    }

    @Test(expected = CommandException.class)
    public void closeOutputStreamThrowsException(@Mocked final FileOutputStream fos) throws Exception {
        prepareMetadata();
        new NonStrictExpectations() {{
            new FileOutputStream(downloadedFile); result = fos;
            fos.close();
            result = new IOException();
        }};
        new DownloadObjectToFileCommandImpl(this.account, httpClient, defaultAccess,
                getObject("objectname"), new DownloadInstructions(), downloadedFile).call();
    }

}
