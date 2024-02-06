package com.aat.application.core.form;

import com.aat.application.annotations.ContentDisplayedInSelect;
import com.aat.application.annotations.DisplayName;
import com.vaadin.flow.router.PageTitle;
import jakarta.persistence.Id;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Class definition to create the mapping for the timeline
 */
public class GridViewParameter {

    private Class<?> entityClass;
    private Class<?> groupClass;
    private Class<?> filterClass;
    /**
     * Headers of the column to display in the content
     * e.g  e.employeeName
     */
    private List<String> headers;
    private String primaryIdFieldName;
    LinkedHashMap<String, String> headerOptions = new LinkedHashMap<>();
    Dictionary<String, String> headerNames = new Hashtable<>();
    Dictionary<String, Class<?>> headerTypeOptions = new Hashtable<>();
    String fieldDisplayedInSelect;
    String groupName;
    String pageName;
    Object[] parameters;

    /**
     * Name of the column for the class
     * Classname is the CSS name to control the appearance of the item
     */
    private String classNameFieldName;

    /**
     * In simple 1 table definition, it's the name of the table
     * but could be more complex if you use a join statement
     * e.g.
     * employee e
     * inner join personalDetails pd on e.empId = pd.empId
     * inner join department d on e.departmentId = d.departmentId
     */
    private String fromDefinition = null;

    /**
     * Add where condition if applicable
     * e.g. d.orgId = ?
     */
    private String whereDefinition = null;

    /**
     * Add where condition if applicable
     * e.g. d.orgId = ?
     */
    private String selectDefinition = null;

    /**
     * This is to test if the definition is valid or not
     * this assists the developer if the call is valid
     *
     * @return boolean
     */
    public boolean isValid() {
        boolean valid = headers != null;
        //TODO - add some logging or notification to developer for missing definition

        if (fromDefinition == null) {
            valid = false;
            //TODO - add some logging or notification to developer for missing definition
        }

        return valid;

    }

    /**
     * This check if it requires filter parameter
     *
     * @return boolean
     */
    public boolean isRequireParameter() {
        return whereDefinition != null;
    }

    public GridViewParameter(Class<?> entityClass, String classNameFieldName) {
        this.classNameFieldName = classNameFieldName;
        this.setEntityClass(entityClass);
        this.headers = configureHeader(entityClass);
        this.fromDefinition = entityClass.getSimpleName();
    }

    private List<String> configureHeader(Class<?> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();

        List<String> fieldNames = new ArrayList<>();
        for (Field field : fields) {
            if (field.getAnnotation(Id.class) != null) {
                fieldNames.add("id");
                this.primaryIdFieldName = field.getName();
                headerNames.put("id", "ID");
            }
            if (field.getAnnotation(DisplayName.class) == null) {
                continue;
            }
            if (field.getAnnotation(ContentDisplayedInSelect.class) != null) {
                fieldDisplayedInSelect = field.getName();
            }
            if (field.getAnnotation(jakarta.persistence.Column.class) != null) {
                fieldNames.add(field.getName());
                if (field.getType().getSimpleName().equals("LocalDateTime"))
                    headerOptions.put(field.getName(), "date");
                else if (field.getType().getSimpleName().equals("boolean"))
                    headerOptions.put(field.getName(), "check");
                else
                    headerOptions.put(field.getName(), "input");
                headerNames.put(field.getName(), field.getAnnotation(DisplayName.class).value());
            }
            if (field.getAnnotation(jakarta.persistence.Enumerated.class) != null) {
                fieldNames.add(field.getName());
                headerTypeOptions.put(field.getName(), field.getType());
                headerNames.put(field.getName(), field.getAnnotation(DisplayName.class).value());
                headerOptions.put(field.getName(), "select_enum");
            }
            if (field.getAnnotation(jakarta.persistence.JoinColumn.class) != null) {
                fieldNames.add(field.getName());
                headerNames.put(field.getName(), field.getAnnotation(DisplayName.class).value());
                headerOptions.put(field.getName(), field.getType().getName());
            }
        }

        return fieldNames;
    }

    public String getPrimaryIdFieldName() {
        return primaryIdFieldName;
    }

    public void setPrimaryIdFieldName(String primaryIdFieldName) {
        this.primaryIdFieldName = primaryIdFieldName;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(Class<?> entityClass) {
        this.entityClass = entityClass;
        pageName = entityClass.getAnnotation(PageTitle.class).value();
        this.headers = configureHeader(entityClass);
        this.fromDefinition = entityClass.getSimpleName();
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public LinkedHashMap<String, String> getHeaderOptions() {
        return headerOptions;
    }

    public void setHeaderOptions(LinkedHashMap<String, String> headerOptions) {
        this.headerOptions = headerOptions;
    }

    public Dictionary<String, String> getHeaderNames() {
        return headerNames;
    }

    public void setHeaderNames(Dictionary<String, String> headerNames) {
        this.headerNames = headerNames;
    }

    public Dictionary<String, Class<?>> getHeaderTypeOptions() {
        return headerTypeOptions;
    }

    public void setHeaderTypeOptions(Dictionary<String, Class<?>> headerTypeOptions) {
        this.headerTypeOptions = headerTypeOptions;
    }

    public String getFieldDisplayedInSelect() {
        return fieldDisplayedInSelect;
    }

    public void setFieldDisplayedInSelect(String fieldDisplayedInSelect) {
        this.fieldDisplayedInSelect = fieldDisplayedInSelect;
    }

    public String getClassNameFieldName() {
        return classNameFieldName;
    }

    public void setClassNameFieldName(String classNameFieldName) {
        this.classNameFieldName = classNameFieldName;
    }

    public String getSelectDefinition() {
        return selectDefinition;
    }

    public void setSelectDefinition(String selectDefinition) {
        this.selectDefinition = selectDefinition;
    }

    public String getFromDefinition() {
        return fromDefinition;
    }

    public void setFromDefinition(String fromDefinition) {
        this.fromDefinition = fromDefinition;
    }

    public String getWhereDefinition() {
        return whereDefinition;
    }

    public void setWhereDefinition(String whereDefinition) {
        this.whereDefinition = whereDefinition;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public Class<?> getGroupClass() {
        return groupClass;
    }

    public void setGroupClass(Class<?> groupClass) {
        this.groupClass = groupClass;
        for (Field field : this.entityClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.getType().getSimpleName().equals(this.groupClass.getSimpleName())) {
                groupName = field.getName();
                break;
            }
        }
    }

    public Class<?> getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(Class<?> filterClass) {
        this.filterClass = filterClass;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
}