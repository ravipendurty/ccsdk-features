/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.about.AboutHttpServlet;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestAbout {

    private static final String REPO_MDSAL_DIR = "system/org/opendaylight/mdsal/mdsal-binding-api/3.0.9/";
    private static final String REPO_YANGTOOLS_DIR = "system/org/opendaylight/yangtools/odl-yangtools-common/2.1.11";

    @BeforeClass
    public static void before() throws IOException {
        //create temporary odl folder structure in tmp
        Files.createDirectories(new File(REPO_MDSAL_DIR).toPath());
        Files.createDirectories(new File(REPO_YANGTOOLS_DIR).toPath());
    }

    @AfterClass
    public static void after() throws IOException {
        //delete created dirs
        delete(new File("system/"));
    }

    private static void delete(File file) throws IOException {

        for (File childFile : file.listFiles()) {

            if (childFile.isDirectory()) {
                delete(childFile);
            } else {
                if (!childFile.delete()) {
                    throw new IOException();
                }
            }
        }

        if (!file.delete()) {
            throw new IOException();
        }
    }

    @Test
    public void testReadmeRequest() throws IOException, ServletException {
        AboutHelperServlet servlet = new AboutHelperServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn("/about");
        StringWriter out = new StringWriter();
        ServletOutputStream printOut = new ServletOutputStream() {

            @Override
            public void write(int arg0) throws IOException {
                out.write(arg0);
            }
        };
        when(response.getOutputStream()).thenReturn(printOut);
        servlet.doGet(request, response);
        verify(response).setStatus(200);
        verify(response).setContentType("text/plain");
        System.out.println(out.getBuffer().toString());
        assertTrue(out.getBuffer().length() > 0);
    }

    @Test
    public void testReadmeResourceRequest() throws IOException, ServletException {
        AboutHelperServlet servlet = new AboutHelperServlet();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getRequestURI()).thenReturn("/about/test.bmp");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ServletOutputStream printOut = new ServletOutputStream() {

            @Override
            public void write(int arg0) throws IOException {
                out.write(arg0);
            }
        };
        when(response.getOutputStream()).thenReturn(printOut);
        servlet.doGet(request, response);
        verify(response).setStatus(200);
        verify(response).setContentType("image/bmp");
        assertTrue(out.size() > 0);
    }



    private class AboutHelperServlet extends AboutHttpServlet {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doGet(req, resp);
        }

    }
}
