package com.jo.application.core.form;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jo.application.annotations.ContentDisplayedInSelect;
import com.jo.application.core.component.TwinColSelect;
import com.jo.application.core.data.entity.ZJTEntity;
import com.jo.application.core.data.service.ZJTService;
import com.jo.application.data.entity.ZJTTableInfo;
import com.jo.application.data.service.TableInfoService;
import com.jo.application.util.GlobalData;
import com.vaadin.componentfactory.tuigrid.TuiGrid;
import com.vaadin.componentfactory.tuigrid.model.*;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.io.Serial;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
@CssImport(value = "./styles/grid.css")
public abstract class StandardForm<T extends ZJTEntity, S extends ZJTService> extends CommonForm {

    @Serial
    private static final long serialVersionUID = -5183438338263448739L;
    private final GridViewParameter gridViewParameter;

    protected TableInfoService tableInfoService;
    String fieldDisplayedInSelect;
    private ZJTTableInfo tableInfo;
    List<Integer> colWidthsResized;
    protected TextField filterText = new TextField();
    private final Button btnReload = new Button("Reload");
    private final Button btnSave = new Button("Save");
    protected Button btnColumnSettings;
    private Dialog twinColSelDialog;
    TwinColSelect twinColSelect;
    Set<String> selectedItems;
    protected S service;
    List<Item> items = new ArrayList<>();
    private boolean bSavedWidth = true;
    private final HorizontalLayout toolbar = new HorizontalLayout();
    private final HorizontalLayout statusBar = new HorizontalLayout();
    private final Button btnInfo = new Button();
    private final TextField lblMessage = new TextField();
    private final Button lblRowCount = new Button();
    private AATContextMenu contextMenu;
    private final List<String> filteredValue = new ArrayList<>();

    public StandardForm(GridViewParameter gridViewParameter,
                        S service, TableInfoService tableInfoService) {
        addClassName("demo-app-form");
        this.gridViewParameter = gridViewParameter;
        this.service = service;
        this.tableInfoService = tableInfoService;
        this.setHeight("calc(-130px + 100vh)");
        dateFilterOn = gridViewParameter.getDateFilterOn();

        initColSelDialog();

        addComponentAtIndex(0, toolbar);
        loadGrid();

        addStatusBar();
    }

    private void addStatusBar() {

        HorizontalLayout left = new HorizontalLayout();
        btnInfo.setIcon(new Icon(VaadinIcon.INFO));
        left.setWidthFull();
        left.add(lblMessage);
        lblMessage.setWidthFull();

        statusBar.add(btnInfo, left, lblRowCount);
        btnColumnSettings = new Button();
        btnColumnSettings.setIcon(new Icon(VaadinIcon.TWIN_COL_SELECT));
        btnColumnSettings.addClickListener(e -> {
            selectedItems = twinColSelect.getSelectedItems();
            if (tableInfo.getPageSize() != null)
                twinColSelDialog.getElement().executeJs("var value = $0; console.log(this);  var numberField = this.getElementById('PageSizeNumber'); console.log(numberField); numberField.value = value;", tableInfo.getPageSize().doubleValue());
            twinColSelDialog.open();
        });

        statusBar.add(btnInfo, left, lblRowCount, btnColumnSettings);
        statusBar.setHeight("40px");
        statusBar.setWidthFull();

        lblMessage.setValue("OK");
        lblRowCount.setText("#");
        lblRowCount.setId("rowcount");
        btnInfo.addClickListener(e -> showMessageInDialog());

        addComponentAtIndex(2, statusBar);
    }

    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        if (gridViewParameter.getParameters() != null &&
                (int) gridViewParameter.getParameters()[0] != -1)
            grid.setFilter(gridViewParameter.groupName, "Select");
        grid.setRowCountOnElement("rowcount");
    }

    private void showMessageInDialog() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Status Message");
        dialog.setText(lblMessage.getValue());

        dialog.open();
    }

    private void loadGrid() {
        boolean bAttach = false;
        if (!twinColSelect.getSelectedItems().isEmpty()) {
            if (grid != null) {
                remove(grid);
                bAttach = true;
            }
            configureGrid();
            if (this.contextMenu != null) {
                this.contextMenu.setTarget(grid);
                grid.setContextMenu(this.contextMenu);
            }
        }
        if (!bAttach)
            try {
                getToolbar();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        if (grid != null)
            addComponentAtIndex(1, grid);
    }

    private void initColSelDialog() {
        twinColSelect = new TwinColSelect();
        List<String> tempHeaderNames = new ArrayList<>();
        for (String header : this.gridViewParameter.getHeaders()) {
            tempHeaderNames.add(this.gridViewParameter.headerNames.get(header));
        }

        tableInfo = tableInfoService.findByTableName(this.gridViewParameter.getEntityClass().getSimpleName());
        if (tableInfo == null) {
            tableInfo = new ZJTTableInfo();
            tableInfo.setTable_name(this.gridViewParameter.getEntityClass().getSimpleName());
        }
        String tempHeader = tableInfo.getHeaders();
        List<String> tempDisplayedHeaderNames = tempHeaderNames;
        if (tempHeader != null && !tempHeader.equals("[]")) {
            this.gridViewParameter.setHeaders(
                    Arrays.stream(tempHeader.substring(1, tempHeader.length() - 1).split(","))
                            .map(String::trim).collect(Collectors.toList()));
            tempDisplayedHeaderNames = new ArrayList<>();
            for (String header : this.gridViewParameter.getHeaders()) {
                tempDisplayedHeaderNames.add(this.gridViewParameter.getHeaderNames().get(header));
            }
        }

        List<String> sortedHeaderNames = new ArrayList<>(tempDisplayedHeaderNames);
        for (String header : tempHeaderNames) {
            if (!sortedHeaderNames.contains(header))
                sortedHeaderNames.add(header);
        }

        twinColSelect.setItems(sortedHeaderNames);
        twinColSelect.select(tempDisplayedHeaderNames);

        NativeLabel label = new NativeLabel("Page Size");
        NumberField numberPageSize = new NumberField();
        numberPageSize.setMaxWidth("100px");
        numberPageSize.setId("PageSizeNumber");
        if (tableInfo.getPageSize() != null)
            numberPageSize.setValue(tableInfo.getPageSize().doubleValue());

        NativeLabel fcLabel = new NativeLabel("Number Frozen Column");
        NumberField numberFC = new NumberField();
        numberFC.setMaxWidth("100px");
        numberFC.setId("PageSizeNumber");
        if (tableInfo.getFrozenCount() != null)
            numberFC.setValue(tableInfo.getFrozenCount().doubleValue());
        HorizontalLayout pageSizePanel = new HorizontalLayout(label, numberPageSize, fcLabel, numberFC);
        pageSizePanel.setAlignItems(Alignment.CENTER);

        Button btnOk = new Button("OK");
        Button btnCancel = new Button("Cancel");

        Checkbox autoWidthSave = new Checkbox("Save Column Width", true);
        autoWidthSave.addValueChangeListener(e -> bSavedWidth = e.getValue());
        HorizontalLayout btnPanel = new HorizontalLayout(btnCancel, btnOk, autoWidthSave);
        btnPanel.setAlignItems(Alignment.CENTER);

        ZJTTableInfo finalTableInfo = tableInfo;
        btnOk.addClickListener(e -> {
            List<String> originHeaders = this.gridViewParameter.getHeaders();
//            List<Integer> columnWidths = getColumnWidths();
            List<Integer> allowedWidths = new ArrayList<>();
            List<String> tempTwinItems = new ArrayList<>(twinColSelect.getSelectedItems());
            List<String> tempHeaders = new ArrayList<>();
            if (!tempTwinItems.contains(fieldDisplayedInSelect)
                    && fieldDisplayedInSelect != null)
                tempTwinItems.add(fieldDisplayedInSelect);
            twinColSelect.select(tempTwinItems);
            for (String desiredValue : tempTwinItems) {
                Enumeration<String> keys = this.gridViewParameter.getHeaderNames().keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement();

                    if (this.gridViewParameter.getHeaderNames().get(key).equals(desiredValue)) {
                        tempHeaders.add(key);
                        break;
                    }
                }
            }
            this.gridViewParameter.setHeaders(tempHeaders);

            for (String allowedHeader : this.gridViewParameter.getHeaders()) {
                if (colWidthsResized == null) {
                    allowedWidths = getColumnWidths();
                    break;
                }
                if (originHeaders.contains(allowedHeader)) {
                    int hdrIndex = originHeaders.indexOf(allowedHeader);
                    if(hdrIndex > 0 & colWidthsResized.size() > hdrIndex)
                        allowedWidths.add(colWidthsResized.get(originHeaders.indexOf(allowedHeader)));
                    else allowedWidths.add(0);
                } else allowedWidths.add(0);
            }

            if (bSavedWidth)
                finalTableInfo.setWidths(allowedWidths.toString());
            finalTableInfo.setHeaders(this.gridViewParameter.getHeaders().toString());
            if (numberPageSize.getValue() != null && numberPageSize.getValue() > 0)
                finalTableInfo.setPageSize(numberPageSize.getValue().intValue());
            if (numberFC.getValue() != null && numberFC.getValue() > 0)
                finalTableInfo.setFrozenCount(numberFC.getValue().intValue());
            else
                finalTableInfo.setFrozenCount(null);

            tableInfoService.save(finalTableInfo);
            tableInfo = finalTableInfo;

            if (!this.gridViewParameter.getHeaders().isEmpty())
                this.loadGrid();
            else grid.removeFromParent();

            grid.setRowCountOnElement("rowcount");
            twinColSelDialog.close();
        });

        btnCancel.addClickListener(e -> twinColSelDialog.close());

        twinColSelDialog = new Dialog();
        twinColSelDialog.add(new VerticalLayout(twinColSelect, pageSizePanel, btnPanel));
        twinColSelDialog.setCloseOnEsc(true);
        twinColSelDialog.setCloseOnOutsideClick(true);
    }

    private void configureGrid() {
        grid = new TuiGrid();
        grid.addClassName("scheduler-grid");
        grid.setHeaders(this.gridViewParameter.getHeaders());
        if (this.gridViewParameter.getParameters() != null)
            grid.setFilterId((int) this.gridViewParameter.getParameters()[0]);

        try {
            items = this.getTableData(this.gridViewParameter.getParameters(), false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        grid.setItems(items);

        List<Column> columns = this.getColumns();
        grid.setColumns(columns);

        if (tableInfo.getPageSize() != null)
            grid.setPageSize(tableInfo.getPageSize());
        if (tableInfo.getFrozenCount() != null)
            grid.setFrozenCount(tableInfo.getFrozenCount());
//
//        List<Summary> summaries = List.of(
//                new Summary(columns.get(columns.size() - 1).getColumnBaseOption().getName(), Summary.OperationType.rowcount));

//        grid.setSummaries(summaries);
        grid.setHeaderHeight(100);
//        grid.setSummaryHeight(40);

        grid.setRowHeaders(List.of("checkbox"));
        grid.sethScroll(false);
        grid.setvScroll(false);

        grid.addColumnResizeListener(event -> {
            int colWidth = event.getColWidth();
            String colName = event.getColName();
            if (colWidthsResized == null)
                colWidthsResized = getColumnWidths();

            for (String header : this.gridViewParameter.getHeaders()) {
                if (header.equals(colName))
                    colWidthsResized.set(this.gridViewParameter.getHeaders().indexOf(header), colWidth);
            }
        });

        grid.setAutoSave(true);
        grid.setHeaderHeight(50);
        grid.setSizeFull();
        if (this.gridViewParameter.isReadOnly())
            grid.onDisable();
        else
            grid.onEnable();
        grid.setbAllowDelete(gridViewParameter.isAllowDelete());
        grid.setbAllowInsert(gridViewParameter.isAllowInsert());
        grid.setFieldsAsReadOnly(gridViewParameter.getFieldsAsReadOnly());
        grid.setMultiSelect(gridViewParameter.isMultiSelect());
//        grid.setTableWidth(500);
//        grid.setTableHeight(750);
    }

    public void setContextMenu(AATContextMenu contextMenu) {
        this.contextMenu = contextMenu;
        this.contextMenu.setTarget(grid);
        grid.setContextMenu(this.contextMenu);
    }

    private List<Item> getTableData(Object[] parameters, boolean flushRecords) throws Exception {

        List<Item> TableData = new ArrayList<>();

        if (!gridViewParameter.isValid()) {
            throw new Exception("TuiGrid Definition is not valid.");
        }
        if (gridViewParameter.isRequireParameter() && parameters == null) {
            throw new Exception("Parameters are required, but not set");
        }

        StringBuilder query = new StringBuilder("SELECT p.").append(gridViewParameter.getPrimaryIdFieldName());
        for (String header : gridViewParameter.getHeaders()) {
            String colType = this.gridViewParameter.getHeaderOptions().get(header);
            if (!header.equals("id")) {
                if (!(colType.equals("input") || colType.equals("date")
                        || colType.equals("input_multi") || colType.equals("date_multi")
                        || colType.equals("check") || colType.equals("select_enum") || colType.equals("CustomComponent")))
                    query.append(", COALESCE(p.").append(header).append(", -1)");
                else
                    query.append(", p.").append(header);
            } else
                query.append(", p.").append(header);

        }

        query.append(" FROM ").append(gridViewParameter.getFromDefinition()).append(" as p");

        if (gridViewParameter.getWhereDefinition() != null && (int) parameters[0] != -1) {
            query.append(" WHERE ").append("p.").append(gridViewParameter.getWhereDefinition());
            query.append(" = ").append(parameters[0]);
//            if (dateFilterOn != null) {
//                addConditionWhenFilteringDate(query);
//            }
        } else {
            if (dateFilterOn != null) {
                query.append(" WHERE 1=1");
                addConditionWhenFilteringDate(query);
            }
        }

        for (Object[] data :
                service.findEntityByQuery(query.toString())) {
            List<String> recordData = Arrays.stream(data)
                    .map(obj -> {
                        if (obj instanceof ZJTEntity) {
                            return String.valueOf(((ZJTEntity) obj).getId());
                        } else if (obj instanceof LocalDateTime) {
                            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy h:mm a", Locale.ENGLISH);
                            return ((LocalDateTime) obj).format(inputFormatter);
                        } else if (obj instanceof int[]) {
                            int[] integers = (int[]) obj;
                            StringBuilder builder = new StringBuilder();
                            for (int integer : integers) {
                                builder.append(String.valueOf(integer));
                                builder.append(", "); // Add a separator if needed
                            }
                            return builder.toString();
                        } else if (obj != null && obj.getClass().isEnum()) {
                            return Objects.toString(((Enum<?>)obj).name(), "");
                        } else {
                            return Objects.toString(obj, "");
                        }
                    })
                    .collect(Collectors.toList());
            GuiItem item = new GuiItem(Integer.parseInt(recordData.get(0)), recordData.subList(1, recordData.size()), gridViewParameter.getHeaders());
            TableData.add(item);
        }

        return TableData;
    }

    private List<com.vaadin.componentfactory.tuigrid.model.Column> getColumns() {
        List<com.vaadin.componentfactory.tuigrid.model.Column> columns = new ArrayList<>();
        int nId = 0;

        List<Integer> colWidths = getColumnWidths();

        for (String header : this.gridViewParameter.getHeaders()) {

            Theme inputTheme = new Theme();
            inputTheme.setBorder("1px solid #326f70");
            inputTheme.setBackgroundColor("#66878858");
            inputTheme.setOutline("none");
            inputTheme.setWidth("90%");
            inputTheme.setHeight("100%");
            inputTheme.setOpacity(1);
            inputTheme.setAlign("left");

            String headerName = this.gridViewParameter.getHeaderNames().get(header);
            int headerIndex = this.gridViewParameter.getHeaders().indexOf(header);
            ColumnBaseOption baseOption = new ColumnBaseOption(nId++, headerName, header, (colWidths.size() > headerIndex) ? colWidths.get(headerIndex) : 0, "left", "");
            com.vaadin.componentfactory.tuigrid.model.Column column = new com.vaadin.componentfactory.tuigrid.model.Column(baseOption);
            column.setEditable(true);
            column.setSortable(true);

            column.setSortingType("asc");
            int index = 1;
            if (header.equals("id"))
                continue;

            if (this.gridViewParameter.getHeaderOptions().get(header).split("_").length == 2)
                column.setMultiline(true);

            switch (this.gridViewParameter.getHeaderOptions().get(header)) {
                case "input":
                case "input_multi":
                    column.setType("input");
                    if (this.gridViewParameter.getHeaderOptions().get(header + "_type").equalsIgnoreCase("Number")) {
                        column.getColumnBaseOption().setAlign("right");
                        inputTheme.setAlign("right");
                    }
                    break;
                case "check":
                    column.setType("check");
                    column.getColumnBaseOption().setAlign("center");
                    inputTheme.setAlign("center");
                    break;
                case "CustomComponent":
                    column.setCustomType(this.gridViewParameter.getHeaderOptions().get("CustomComponentName_" + header));
                    column.setType("CustomComponent");
                    column.getColumnBaseOption().setAlign("center");
                    inputTheme.setAlign("center");
                    break;
                case "date":
                case "date_multi":
                    column.setType("datePicker");
                    column.setDateOption(new DateOption("yyyy-MM-dd HH:mm A", true));
                    break;
                case "select_enum":
                    column.setType("select");
                    column.setRoot(true);
                    column.setTarget("");
                    List<RelationOption> elementsList = new ArrayList<>();
                    Class<?> fieldEnum = this.gridViewParameter.getHeaderTypeOptions().get(header);
                    for (Enum<?> elementList : getEnumConstants(fieldEnum)) {
                        RelationOption option = new RelationOption(elementList.toString(), elementList.name());
                        elementsList.add(option);
                    }
                    column.setRelationOptions(elementsList);
                    break;
                default:
                    column.setType("select");
                    column.setRoot(true);
                    column.setTarget("");
                    Class<?> selectClass;
                    try {
                        selectClass = Class.forName(this.gridViewParameter.getHeaderOptions().get(header));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    List<String> annotatedFields = GlobalData.getFieldNamesWithAnnotation(ContentDisplayedInSelect.class, selectClass, true);
                    String pkField = GlobalData.getPrimaryKeyField(selectClass).getName();
                    StringBuilder query = new StringBuilder("SELECT p.").append(pkField);
                    for (String annotatedField : annotatedFields)
                        query.append(", p.").append(annotatedField);

                    query.append(" FROM ").append(selectClass.getSimpleName()).append(" as p");

                    List<RelationOption> options = new ArrayList<>();
                    for (Object[] data :
                            service.findEntityByQuery(query.toString())) {
                        RelationOption option = new RelationOption(makeContent(annotatedFields, data), String.valueOf(data[0]));
                        options.add(option);
                    }
                    column.setRelationOptions(options);
            }
            column.setInputTheme(inputTheme);
            column.setSelectTheme(inputTheme);
            columns.add(column);
        }
        return columns;
    }

    private void getToolbar() throws Exception {
        filterText.setPlaceholder("Filter by name...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateList());
        btnReload.addClickListener(e -> reloadGrid());
        btnSave.addClickListener(e -> saveAll());

        if (this.gridViewParameter.getParameters() != null &&
                (int) this.gridViewParameter.getParameters()[0] != -1) {
            if (!gridViewParameter.isValid()) {
                throw new Exception("TuiGrid Definition is not valid.");
            }
            if (gridViewParameter.isRequireParameter() && gridViewParameter.getSelectDefinition() == null) {
                throw new Exception("Parameters are required, but not set");
            }

            List<String> annotatedFields = GlobalData.getFieldNamesWithAnnotation(ContentDisplayedInSelect.class, gridViewParameter.getFilterClass(), true);
            String pkField = GlobalData.getPrimaryKeyField(gridViewParameter.getFilterClass()).getName();
            StringBuilder query = new StringBuilder("SELECT p.").append(pkField);
            for (String annotatedField : annotatedFields)
                query.append(", p.").append(annotatedField);

            query.append(" FROM ").append(gridViewParameter.getFilterClass().getSimpleName()).append(" as p");

            if (gridViewParameter.getWhereDefinition() != null && (int) gridViewParameter.getParameters()[0] != -1) {
                String[] whereDefinition = gridViewParameter.getWhereDefinition().split("\\.");
                switch (gridViewParameter.getWhereDefinition().split("\\.").length) {
                    case 2:
                        query.append(" WHERE ").append("p.").append(whereDefinition[1]);
                        break;
                    case 3:
                        query.append(" WHERE ").append("p.").append(whereDefinition[1]).append(".").append(whereDefinition[2]);
                        break;
                }
                //TODO -set parameter
                query.append(" = ").append(gridViewParameter.getParameters()[0]);
            }

            for (Object[] data :
                    service.findEntityByQuery(query.toString())) {
                filteredValue.add(makeContent(annotatedFields, data));
            }
        }


//        HorizontalLayout columnToolbar = new HorizontalLayout(autoWidthSave, columns);

        if (dateFilterOn != null)
            toolbar.add(dateFilter);
//        else
//            toolbar.add(filterText, btnReload, btnSave);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.addClassName("aat-toolbar");
    }


    @Override
    public void onUpdateForm() throws Exception {
        if (this.gridViewParameter == null)
            return;

        this.updateList();
    }

    private String makeContent(List<String> annotatedFields, Object[] data) {
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= annotatedFields.size(); i++) {
            if (data[i] instanceof LocalDateTime) {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy h:mm a", Locale.ENGLISH);
                content.append(" - ").append(((LocalDateTime) data[i]).format(inputFormatter));
            } else
                content.append(data[i]);
        }
        return content.toString();
    }

    @Override
    public String getHamburgerText() {
        if (filteredValue.isEmpty()) {
            return "";
        }
        return ">> " + gridViewParameter.getPageName() + filteredValue;
    }

    @Override
    public String getOriginViewText() {
        if (filteredValue.isEmpty())
            return "";
        return GlobalData.convertToStandard(this.gridViewParameter.groupName);
    }

    public void setMessageStatus(String msg) {
        lblMessage.setValue(msg);
    }

    public void addCustomButton(Component component) {
        addCustomComponent(-1, component);
    }
    public void addCustomComponent(int index, Component component) {
        if (index < 0) {
            toolbar.add(component);
        } else {
            toolbar.addComponentAtIndex(index, component);
        }
    }

    public void onNewItem(GuiItem item) {
        if (!gridViewParameter.isAllowInsert())
            return;
        try {
            grid.onEnable();
            grid.setRowCountOnElement("rowcount");
            T entityData = service.addNewEntity(this.gridViewParameter.getEntityClass());
            grid.setIDToGridRow(item.getId(), entityData.getId());
        } catch (RuntimeException e) {
            e.fillInStackTrace();
        }
    }

    public ZJTEntity onNewItem(ZJTEntity entity, int itemId) {
        try {
            grid.setRowCountOnElement("rowcount");
            ZJTEntity entityData = service.addNewEntity(entity);
            grid.setIDToGridRow(itemId, entityData.getId());

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(entity);
            setDataToGridRow(itemId, json);
            return entityData;
        } catch (JsonProcessingException | RuntimeException e) {
            e.fillInStackTrace();
        }
        return null;
    }

    @Override
    public void setDataToGridRow(int itemId, String data) {
        try {
            grid.setDataToGridRow(itemId, data);
        } catch (RuntimeException e) {
            e.fillInStackTrace();
        }
    }

    public int onUpdateItem(Object[] parameters) throws Exception {
        if (!gridViewParameter.isValid()) {
            throw new Exception("TuiGrid Definition is not valid.");
        }
        if (gridViewParameter.isRequireParameter() && parameters == null) {
            throw new Exception("Parameters are required, but not set");
        }
        Class<?> aClass = this.gridViewParameter.getHeaderTypeOptions().get(parameters[1].toString());
        if(aClass != null && aClass.isEnum())
        {
            parameters[2] = createEnumObject(Class.forName(this.gridViewParameter.getHeaderTypeOptions().get(parameters[1].toString()).getName()), parameters[2].toString());
        }
        String colType = this.gridViewParameter.getHeaderOptions().get(parameters[1].toString());
        if (!(colType.equals("input") || colType.equals("date")
                || colType.equals("check") || colType.equals("select_enum") || colType.equals("CustomComponent"))) {
            Class<?> selectClass;
            try {
                selectClass = Class.forName(colType);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            String pkField = GlobalData.getPrimaryKeyField(selectClass).getName();
            parameters[1] = parameters[1] + "." + pkField;
        }

        StringBuilder query = new StringBuilder("UPDATE ")
                .append(gridViewParameter.getFromDefinition());
        query.append(" p SET p.")
                .append(parameters[1]).append(" = ")
                .append(":param1");
        query.append(" WHERE ")
                .append("p.").append(gridViewParameter.getPrimaryIdFieldName())
                .append(" = ")
                .append(":param2");
        grid.setRowCountOnElement("rowcount");

        return service.updateEntityByQuery(query.toString(), parameters);
    }

    public <T extends Enum<T>> T createEnumObject(Class<?> enumClass, String value) {
        try {
            return Enum.valueOf((Class<T>) enumClass, value);
        } catch (Exception e) {
            e.fillInStackTrace();
            return null;
        }
    }

    public int onDeleteItemChecked() throws Exception {
        int[] checkedItems = grid.getCheckedItems();

        if(!gridViewParameter.isAllowDelete())
            return 0;

        if (!gridViewParameter.isValid()) {
            throw new Exception("TuiGrid Definition is not valid.");
        }
        StringBuilder query = new StringBuilder("DELETE FROM ")
                .append(gridViewParameter.getFromDefinition())
                .append(" p WHERE ");
        for (int i = 0; i < checkedItems.length; i++) {
            if (i != 0)
                query.append(" OR ");
            query.append("p.").append(gridViewParameter.getPrimaryIdFieldName())
                    .append(" = ")
                    .append(checkedItems[i]);
        }

        grid.setRowCountOnElement("rowcount");
        return service.deleteEntityByQuery(query.toString());
    }

    private void saveAll() {
    }

    public void reloadGrid() {
        grid.reloadData();
    }

    public void restore() {
        grid.refreshGrid();
    }

    private List<Integer> getColumnWidths() {
        String strWidths = tableInfo.getWidths();
        List<Integer> colWidths = new ArrayList<>();

        if (strWidths == null || strWidths.isEmpty() || strWidths.equals("[]")) {
            for (String ignored : this.gridViewParameter.getHeaders()) {
                colWidths.add(0);
            }
        } else {
            String[] elements = strWidths.substring(1, strWidths.length() - 1).split(", ");
            for (String element : elements) {
                colWidths.add(Integer.parseInt(element));
            }
        }

        return colWidths;
    }

    public void updateList() {
        try {
            grid.setItems(this.getTableData(gridViewParameter.getParameters(), false));
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        grid.reloadData();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Enum<T>> Enum<T>[] getEnumConstants(Class<?> enumTypes) {
        return ((Class<T>) enumTypes).getEnumConstants();
    }
}