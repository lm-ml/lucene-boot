package com.flight.service.impl;

import com.flight.service.LuceneService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class LuceneServiceImplTest {

    @InjectMocks
    private LuceneService luceneService = new LuceneServiceImpl();

    @Mock
    private ResourceLoader resourceLoader;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_return_null_when_call_getFileByResourceName_and_resourceName_isnull() throws IOException {
        File file = luceneService.getFileByResourceName(null);
        assertNull(file);
        verify(resourceLoader, never()).getResource(anyString());
    }

    @Test
    public void should_return_null_when_call_getFileByResourceName_and_resourceName_is_not_null_and_resource_is_notExists() throws IOException {
        String resourceName = "resourceName";
        Resource resource = mock(Resource.class);
        doReturn(false).when(resource).exists();
        doReturn(resource).when(resourceLoader).getResource(anyString());
        File file = luceneService.getFileByResourceName(resourceName);
        assertNull(file);
        ArgumentCaptor<String> resourceNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(resourceLoader).getResource(resourceNameCaptor.capture());
        assertEquals("classpath:data/" + resourceName, resourceNameCaptor.getValue());
    }

    @Test
    public void should_return_file_when_call_getFileByResourceName_and_resourceName_is_not_null_and_resource_isExists() throws IOException {
        String resourceName = "resourceName";
        Resource resource = mock(Resource.class);
        doReturn(true).when(resource).exists();
        File file = mock(File.class);
        doReturn(resourceName).when(file).getName();
        doReturn(file).when(resource).getFile();
        doReturn(resource).when(resourceLoader).getResource(anyString());
        File resourceFile = luceneService.getFileByResourceName(resourceName);
        assertEquals(resourceName, resourceFile.getName());
    }


}
