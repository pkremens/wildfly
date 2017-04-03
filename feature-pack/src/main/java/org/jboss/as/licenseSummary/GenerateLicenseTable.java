/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.licenseSummary;

import com.sun.media.jfxmedia.logging.Logger;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;

public class GenerateLicenseTable {

    public static void main(String[] argv) {
        try {
            if(argv.length==4) {
                File stylesheet = new File(argv[0]);
                File xmlfile  = new File(argv[1]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document document = db.parse(xmlfile);
                StreamSource stylesource = new StreamSource(stylesheet);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer(stylesource);
                transformer.setParameter("version", argv[3]);
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                transformer.setParameter("timestamp", dateFormat.format(date));
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(argv[2]);
                transformer.transform(source,result);
            }else{
                Logger.logMsg(Logger.INFO, "Not all arguments are provided. The number of arguments needed is tree.");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
