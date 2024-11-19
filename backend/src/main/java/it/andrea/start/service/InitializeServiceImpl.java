package it.andrea.start.service;

import static it.andrea.start.constants.ApplicationConstants.SYSTEM_ENTITY_MANAGE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import it.andrea.start.constants.ApplicationConstants;
import it.andrea.start.constants.UserStatus;
import it.andrea.start.exception.BusinessException;
import it.andrea.start.exception.MappingToDtoException;
import it.andrea.start.exception.MappingToEntityException;
import it.andrea.start.exception.user.UserNotFoundException;
import it.andrea.start.models.JobInfo;
import it.andrea.start.models.user.User;
import it.andrea.start.models.user.UserRole;
import it.andrea.start.repository.JobInfoRepository;
import it.andrea.start.repository.user.UserRepository;
import it.andrea.start.repository.user.UserRoleRepository;
import it.andrea.start.security.IEncrypterManager;

@Service
@Transactional
public class InitializeServiceImpl implements InitializeService {

    private static final Logger LOG = LoggerFactory.getLogger(InitializeServiceImpl.class);

    private final String appPath;

    private static final String XML_FILE = ".xml";

    private static final String JOBS_FILE = "jobs" + XML_FILE;
    private static final String USERS_FILE = "users" + XML_FILE;
    
    private final IEncrypterManager encrypterManager;
    
    private final DocumentBuilderFactory documentBuilderFactory;

    private final JobInfoRepository jobInfoRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public InitializeServiceImpl(Environment environment, IEncrypterManager encrypterManager, JobInfoRepository jobInfoRepository, UserRepository userRepository, UserRoleRepository userRoleRepository, DocumentBuilderFactory documentBuilderFactory) {
        super();
        this.appPath = environment.getProperty("app.initialize.file.path");
        this.encrypterManager = encrypterManager;
        this.jobInfoRepository = jobInfoRepository;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.documentBuilderFactory = documentBuilderFactory;
    }

    @Override
    public void executeStartOperation() throws UserNotFoundException, MappingToDtoException, MappingToEntityException, BusinessException {
        addRoles();
        loadJobsUsersFromXML(appPath + USERS_FILE);
        loadJobsFromXML(appPath + JOBS_FILE);
    }

    private void addRoles() {
        this.addDefaultRole(ApplicationConstants.SYSTEM_ROLE_ADMIN, "Role administrator");
        this.addDefaultRole(ApplicationConstants.SYSTEM_ROLE_MANAGER_ANNOTATION, "Role manager");
        this.addDefaultRole(ApplicationConstants.SYSTEM_ROLE_SUPERVISOR_ANNOTATION, "Role supervisor");
        this.addDefaultRole(ApplicationConstants.SYSTEM_ROLE_OPERATOR, "Role operator");
    }

    private void addDefaultRole(String id, String description) {
        Optional<UserRole> existingRole = userRoleRepository.findByRole(id);
        if (existingRole.isEmpty()) {
            UserRole userRole = new UserRole();
            userRole.setRole(id);
            userRole.setRoleDescription(description);
            userRoleRepository.save(userRole);
        }
    }

    private void loadJobsUsersFromXML(String filePath) {
        try {
            File file = new File(filePath);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("users");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    User user = createUserFromElement(element);
                    if (userRepository.findByUsername(user.getUsername()).isEmpty()) {
                        user.setCreator(SYSTEM_ENTITY_MANAGE);
                        user.setLastModifiedBy(SYSTEM_ENTITY_MANAGE);
                        
                        userRepository.save(user);
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logXmlError(e);
        } catch (Exception e) {
            logUnexpectedError(e);
        }
    }

    private User createUserFromElement(Element element) {
        User user = new User();
        user.setUsername(getTagValue("username", element));
        user.setName(getTagValue("name", element));
        user.setPassword(encrypterManager.encode(getTagValue("password", element)));
        user.setEmail(getTagValue("email", element));
        user.setUserStatus(UserStatus.valueOf(getTagValue("userStatus", element)));

        Collection<UserRole> userRoles = new ArrayList<>();
        NodeList rolesList = element.getElementsByTagName("role");
        for (int i = 0; i < rolesList.getLength(); i++) {
            Node roleNode = rolesList.item(i);
            if (roleNode.getNodeType() == Node.ELEMENT_NODE) {
                Element roleElement = (Element) roleNode;
                String roleName = getTagValue("roleName", roleElement);
                Optional<UserRole> userRoleOpt = userRoleRepository.findByRole(roleName);
                if(userRoleOpt.isPresent()) {
                	userRoles.add(userRoleOpt.get());                	
                }
            }
        }
        user.setRoles(userRoles);

        user.setLanguageDefault(getTagValue("languageDefault", element));

        return user;
    }

    private void loadJobsFromXML(String filePath) {
        try {
            File file = new File(filePath);
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("job");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    JobInfo jobInfo = createJobInfoFromElement(element);

                    jobInfoRepository.save(jobInfo);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logXmlError(e);
        } catch (Exception e) {
            logUnexpectedError(e);
        }
    }

    private JobInfo createJobInfoFromElement(Element element) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setJobName(getTagValue("name", element));
        jobInfo.setDescription(getTagValue("description", element));
        jobInfo.setJobGroup(getTagValue("group", element));
        jobInfo.setJobClass(getTagValue("class", element));
        jobInfo.setCronExpression(getTagValue("cronExpression", element));
        jobInfo.setRepeatTime(parseLongOrDefault(getTagValue("repeatTime", element), 0L));
        jobInfo.setRepeatCount(parseIntOrDefault(getTagValue("repeatCount", element), 0));
        jobInfo.setCronJob(parseBooleanOrDefault(getTagValue("cronJob", element), false));
        jobInfo.setIsActive(parseBooleanOrDefault(getTagValue("isActive", element), false));
        return jobInfo;
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : null;
    }

    private Long parseLongOrDefault(String value, Long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logNumberFormatError("long", value, e);
            return defaultValue;
        }
    }

    private Integer parseIntOrDefault(String value, Integer defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logNumberFormatError("int", value, e);
            return defaultValue;
        }
    }

    private Boolean parseBooleanOrDefault(String value, Boolean defaultValue) {
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }

    private void logXmlError(Exception e) {
        LOG.error("Error processing XML document", e);
    }

    private void logUnexpectedError(Exception e) {
        LOG.error("Unhandled exception while loading jobs from XML", e);
    }

    private void logNumberFormatError(String type, String value, NumberFormatException e) {
        LOG.warn("Cannot convert to {}: {}", type, value, e);
    }

}
