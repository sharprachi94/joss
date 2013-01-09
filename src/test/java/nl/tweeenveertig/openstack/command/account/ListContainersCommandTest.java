package nl.tweeenveertig.openstack.command.account;

import nl.tweeenveertig.openstack.command.core.BaseCommandTest;
import nl.tweeenveertig.openstack.exception.CommandException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ListContainersCommandTest extends BaseCommandTest {

    @Before
    public void setup() throws IOException {
        super.setup();
    }

    @Test
    public void listContainers() throws IOException {
        new ListContainersCommand(this.account, httpClient, defaultAccess, null, 10).call();
    }

    @Test
    public void listContainersWithNoneThere() throws IOException {
        when(statusLine.getStatusCode()).thenReturn(204);
        new ListContainersCommand(this.account, httpClient, defaultAccess, null, 10).call();
    }

    @Test (expected = CommandException.class)
    public void unknownError() throws IOException {
        checkForError(500, new ListContainersCommand(this.account, httpClient, defaultAccess, null, 10));
    }

    @Test
    public void isSecure() throws IOException {
        isSecure(new ListContainersCommand(this.account, httpClient, defaultAccess, null, 10));
    }

    @Test
    public void queryParameters() throws IOException {
        when(statusLine.getStatusCode()).thenReturn(204);
        new ListContainersCommand(this.account, httpClient, defaultAccess, "dogs", 10).call();
        verify(httpClient).execute(requestArgument.capture());
        String assertQueryParameters = "?marker=dogs&limit=10";
        String uri = requestArgument.getValue().getURI().toString();
        assertTrue(uri+" must contain "+assertQueryParameters, uri.contains(assertQueryParameters));
    }

}
