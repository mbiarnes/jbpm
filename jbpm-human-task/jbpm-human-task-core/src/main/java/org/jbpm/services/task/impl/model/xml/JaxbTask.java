package org.jbpm.services.task.impl.model.xml;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jbpm.services.task.impl.model.xml.adapter.I18NTextXmlAdapter;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalI18NText;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;
import org.kie.internal.task.api.model.InternalPeopleAssignments;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.internal.task.api.model.InternalTaskData;

@XmlRootElement(name="task")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbTask implements Task {

    @XmlElement
    @XmlSchemaType(name="long")
    private Long id;

    @XmlElement
    @XmlSchemaType(name="long")
    private Integer priority;

    @XmlElement(name="task-type")
    @XmlSchemaType(name="int")
    private String taskType; 

    @XmlElement(name="name")
    @XmlJavaTypeAdapter(value=I18NTextXmlAdapter.class)
    private List<I18NText> names;
    
    @XmlElement(name="subject")
    @XmlJavaTypeAdapter(value=I18NTextXmlAdapter.class)
    private List<I18NText> subjects;
    
    @XmlElement(name="description")
    @XmlJavaTypeAdapter(value=I18NTextXmlAdapter.class)
    private List<I18NText> descriptions;
    
    @XmlElement
    private JaxbPeopleAssignments peopleAssignments;
    
    @XmlElement
    private JaxbTaskData taskData;
    
    public JaxbTask() { 
        // Default constructor
    }
    
    public JaxbTask(Task task) { 
        initialize(task);
    }
    
    public void initialize(Task task) { 
        if( task == null ) { 
            return;
        }
        this.id = task.getId();
        this.priority = task.getPriority();
        this.peopleAssignments = new JaxbPeopleAssignments(task.getPeopleAssignments());

        // Done because we get a (lazy-initialized) entity back from the task service, which causes problems outside a tx
        // .. so we "eager-initialize" all values here to avoid problems later. (Also in JaxbPeopleAssignments)
        // Collection.toArray() == PersistenceBag.toArray(), which calls PersistenceBag.read(), initializing collection
        // See org.hibernate.collection.internal.PersistenceBag
        this.names = task.getNames();
        this.names.toArray();
        this.subjects = task.getSubjects();
        this.subjects.toArray();
        this.descriptions = task.getDescriptions();
        this.descriptions.toArray();
        
        this.taskData = new JaxbTaskData(task.getTaskData());
        this.taskType = task.getTaskType();
    }
    
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public List<I18NText> getNames() {
        if( names == null ) { 
            names = Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(names);
    }

    @Override
    public List<I18NText> getSubjects() {
        if( subjects == null ) { 
            subjects = Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(subjects);
    }

    @Override
    public List<I18NText> getDescriptions() {
        if( descriptions == null ) { 
            descriptions = Collections.EMPTY_LIST;
        }
        return Collections.unmodifiableList(descriptions);
    }

    @Override
    public PeopleAssignments getPeopleAssignments() {
        return peopleAssignments;
    }

    @Override
    public TaskData getTaskData() {
        return taskData;
    }

    @Override
    public String getTaskType() {
        return taskType;
    }

    public Task getTask() { 
        Task taskImpl = TaskModelProvider.getFactory().newTask();
        List<I18NText> names = new ArrayList<I18NText>();
        for (I18NText n: this.getNames()) {
        	I18NText text = TaskModelProvider.getFactory().newI18NText();
        	((InternalI18NText) text).setLanguage(n.getLanguage());
        	((InternalI18NText) text).setText(n.getText());
            names.add(text);
        }
        ((InternalTask)taskImpl).setNames(names);
        List<I18NText> descriptions = new ArrayList<I18NText>();
        for (I18NText n: this.getDescriptions()) {
        	I18NText text = TaskModelProvider.getFactory().newI18NText();
        	((InternalI18NText) text).setLanguage(n.getLanguage());
        	((InternalI18NText) text).setText(n.getText());
            names.add(text);
        }
        ((InternalTask)taskImpl).setDescriptions(descriptions);
        List<I18NText> subjects = new ArrayList<I18NText>();
        for (I18NText n: this.getSubjects()) {
        	I18NText text = TaskModelProvider.getFactory().newI18NText();
        	((InternalI18NText) text).setLanguage(n.getLanguage());
        	((InternalI18NText) text).setText(n.getText());
            names.add(text);
        }
        ((InternalTask)taskImpl).setSubjects(subjects);
        ((InternalTask)taskImpl).setPriority(this.getPriority());
        InternalTaskData taskData = (InternalTaskData) TaskModelProvider.getFactory().newTaskData();
        taskData.setWorkItemId(this.getTaskData().getWorkItemId());
        taskData.setProcessInstanceId(this.getTaskData().getProcessInstanceId());
        taskData.setProcessId(this.getTaskData().getProcessId());
        taskData.setProcessSessionId(this.getTaskData().getProcessSessionId());
        taskData.setSkipable(this.getTaskData().isSkipable());
        PeopleAssignments peopleAssignments = TaskModelProvider.getFactory().newPeopleAssignments();
        List<OrganizationalEntity> potentialOwners = new ArrayList<OrganizationalEntity>();
        for (OrganizationalEntity e: this.getPeopleAssignments().getPotentialOwners()) {
            if (e instanceof User) {
            	User user = TaskModelProvider.getFactory().newUser();
            	((InternalOrganizationalEntity) user).setId(e.getId());
                potentialOwners.add(user);
            } else if (e instanceof Group) {
            	Group group = TaskModelProvider.getFactory().newGroup();
            	((InternalOrganizationalEntity) group).setId(e.getId());
                potentialOwners.add(group);
            }
        }
        ((InternalPeopleAssignments)peopleAssignments).setPotentialOwners(potentialOwners);
        List<OrganizationalEntity> businessAdmins = new ArrayList<OrganizationalEntity>();
        for (OrganizationalEntity e: this.getPeopleAssignments().getBusinessAdministrators()) {
            if (e instanceof User) {
            	User user = TaskModelProvider.getFactory().newUser();
            	((InternalOrganizationalEntity) user).setId(e.getId());
                businessAdmins.add(user);
            } else if (e instanceof Group) {
            	Group group = TaskModelProvider.getFactory().newGroup();
            	((InternalOrganizationalEntity) group).setId(e.getId());
                businessAdmins.add(group);
            }
        }
        if (this.getPeopleAssignments().getTaskInitiator() != null) {
        	User user = TaskModelProvider.getFactory().newUser();
        	((InternalOrganizationalEntity) user).setId(this.getPeopleAssignments().getTaskInitiator().getId());
        	((InternalPeopleAssignments)peopleAssignments).setTaskInitiator(user);
        }
        ((InternalPeopleAssignments)peopleAssignments).setBusinessAdministrators(businessAdmins);
        ((InternalPeopleAssignments)peopleAssignments).setExcludedOwners(new ArrayList<OrganizationalEntity>());
        ((InternalPeopleAssignments)peopleAssignments).setRecipients(new ArrayList<OrganizationalEntity>());
        ((InternalPeopleAssignments)peopleAssignments).setTaskStakeholders(new ArrayList<OrganizationalEntity>());
        ((InternalTask)taskImpl).setPeopleAssignments(peopleAssignments);        
        ((InternalTask)taskImpl).setTaskData(taskData);
       
        return taskImpl;
    }
    
    @Override
    public void readExternal(ObjectInput arg0) throws IOException, ClassNotFoundException {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + Task.class.getSimpleName() + " implementation.");
    }

    @Override
    public void writeExternal(ObjectOutput arg0) throws IOException {
        String methodName = (new Throwable()).getStackTrace()[0].getMethodName();
        throw new UnsupportedOperationException( methodName + " is not supported on the JAXB " + Task.class.getSimpleName() + " implementation.");
    }

}
