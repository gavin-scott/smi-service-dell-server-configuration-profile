package com.dell.isg.smi.service.server.configuration.manager;

import com.dell.isg.smi.service.server.configuration.model.*;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.*;

public class ConfigurationManagerImplTest {
    private IConfigurationManager configManager;

    @Before
    public void setup() {
        ConfigurationManagerImpl impl = new ConfigurationManagerImpl();
        // TODO: figure out how to get this auto-wired
        impl.setComponentPredicate(new ComponentPredicate());
        configManager = impl;
    }

    @Test
    public void testUpdateComponents() throws Exception {
        URL origFile = Thread.currentThread().getContextClassLoader().getResource("exported-scp-1.xml");
        assertNotNull("Failed to load exported-scp-1.xml", origFile);

        Path tempFile = Files.createTempFile("ConfigurationManagerImplTest", "testIt");
        try {
            Files.copy(new File(origFile.getPath()).toPath(), tempFile, REPLACE_EXISTING);

            ComponentList request = new ComponentList();
            ServerAndNetworkShareRequest shareRequest = new ServerAndNetworkShareRequest();
            shareRequest.setFilePathName(tempFile.toString());
            request.setServerAndNetworkShareRequest(shareRequest);
            List<ServerComponent> serverComponents = new ArrayList<>();

            ServerComponent component = new ServerComponent();
            component.setFQDD("BIOS.Setup.1-1");
            component.setAttributes(new ArrayList<>());
            Attribute attr = new Attribute();
            attr.setName("MemTest");
            attr.setValue("Enabled");
            component.getAttributes().add(attr);
            serverComponents.add(component);

            request.setServerComponents(serverComponents);

            configManager.updateComponents(request);

            // TODO: verify file got written with MemTest changed
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    Node textToNode(DocumentBuilder documentBuilder, String text) throws IOException, SAXException {
        Document document = documentBuilder.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
        return document.getDocumentElement();
    }

    @Test
    public void testUpdateCommentedOutComponents() throws Exception {


        URL origFile = Thread.currentThread().getContextClassLoader().getResource("exported-scp-2.xml");
        assertNotNull("Failed to load exported-scp-2.xml", origFile);

        Path tempFile = Files.createTempFile("ConfigurationManagerImplTest", "testIt");
        try {
            Files.copy(new File(origFile.getPath()).toPath(), tempFile, REPLACE_EXISTING);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document document = db.parse(new File(tempFile.toString()));
            JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[] { SystemConfiguration.class },
                    null);

            Binder<Node> binder = jaxbContext.createBinder();
            binder.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            binder.setProperty(Marshaller.JAXB_FRAGMENT, true);

            SystemConfiguration systemConfig = (SystemConfiguration) binder.unmarshal(document);

            for (ServerComponent component : systemConfig.getServerComponents()) {
                if ("BIOS.Setup.1-1".equals(component.getFQDD())) {
                    Node componentNode = binder.getXMLNode(component);
                    NodeList childNodes = componentNode.getChildNodes();
                    for (int i = 0; i < childNodes.getLength(); ++i) {
                        Node n = childNodes.item(i);
                        if (n.getNodeType() == Node.COMMENT_NODE) {
                            String textContent = n.getTextContent();
                            System.out.println(textContent);

                            Node uncommented = textToNode(db, textContent);
                            System.out.println("UNCOMMENTED = " + uncommented);

                            Attribute attribute = (Attribute) binder.unmarshal(uncommented);
                            System.out.println("ATTRIBUTE = " + attribute);

                            if ("SecureBoot".equals(attribute.getName())) {
                                Node newChild = document.importNode(uncommented, true);
                                componentNode.insertBefore(newChild, n);
                                componentNode.removeChild(n);
                            }
                        }
                    }
                }
            }

//            Node node = binder.updateXML(systemConfig);
//            binder.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//            document.setNodeValue(node.getNodeValue());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            transformer.transform(source, streamResult);

            System.out.println(stringWriter.toString());

            // TODO: verify file got written with MemTest changed
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}