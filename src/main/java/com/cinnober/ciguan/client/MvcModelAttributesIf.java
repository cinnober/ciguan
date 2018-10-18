/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Cinnober Financial Technology AB (cinnober.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.cinnober.ciguan.client;

/**
 * Interface containing attribute names for all models
 */
public interface MvcModelAttributesIf {

    // Normal attributes
    String ATTR_ACC_SERVICE = "accService";
    String ATTR_ACCOUNT = "account";
    String ATTR_ACTING_USER_ID = "actingUserId";
    String ATTR_ACTIVE = "active";
    String ATTR_ADD_VIEW = "addView";
    String ATTR_ALIGN = "align";
    String ATTR_ALLOW_MULTI_EXPANSION = "allowMultiExpansion";
    String ATTR_ALLOW_REMOVE = "allowRemove";
    String ATTR_ARRAY_ITEMS = "arrayItems";
    String ATTR_AS_LOCALE = "asLocale";
    String ATTR_ATTRIBUTES = "attributes";
    String ATTR_AUTHOR = "author";
    String ATTR_AUTO_SUBMIT = "autoSubmit";
    String ATTR_AXIS = "axis";
    String ATTR_BUSINESS_TYPE = "businessType";
    String ATTR_CANCEL = "cancel";
    String ATTR_CLASSNAME = "className";
    String ATTR_CLEAR_DISABLED = "clearDisabled";
    String ATTR_CLIENT_USER_SETTINGS = "clientUserSettings";
    String ATTR_CLOSE = "close";
    String ATTR_CODE = "code";
    String ATTR_COLOR = "color";
    String ATTR_COLUMN_COUNT = "columnCount";
    String ATTR_COLUMN_INDEX = "columnIndex";
    String ATTR_COLUMN_NAME = "columnName";
    String ATTR_COLUMN_NAMES = "columnNames";
    String ATTR_COLUMN_SORTING = "columnSorting";
    String ATTR_COLUMN_SPACING = "columnSpacing";
    String ATTR_COLUMN_WIDTH = "columnWidth";
    String ATTR_COLUMNS = "columns";
    String ATTR_CONDITION = "condition";
    String ATTR_CONFIRM_BUTTONS = "confirmButtons";
    String ATTR_CONFIRM_MESSAGE = "confirmMessage";
    String ATTR_CONSTANT_GROUP_NAME = "constantGroupName";
    String ATTR_CONSTANT_NAME = "constantName";
    String ATTR_CONSTANT_VALUE = "constantValue";
    String ATTR_CONTEXT = "context";
    String ATTR_CONTEXT_MENU_KEY = "contextMenuKey";
    String ATTR_CONTEXT_OBJECT = "contextObject";
    String ATTR_CONTEXT_OBJECTS = "contextObjects";
    String ATTR_CTRL_KEY_DOWN = "ctrlKeyDown";
    String ATTR_CUSTOM_SLOT_TYPE = "customSlotType";
    String ATTR_DATA = "data";
    String ATTR_DATA_TYPE = "dataType";
    String ATTR_DATASOURCE_ID = "dataSourceId";
    String ATTR_DATASOURCE_TYPE = "dataSourceType";
    String ATTR_DATE = "date";
    String ATTR_DECIMAL = "decimal";
    String ATTR_DECIMAL_SEPARATOR = "decimalSeparator";
    String ATTR_DEFAULT = "default";
    String ATTR_DEVICE_TYPE = "deviceType";
    String ATTR_DIALOG_STATE = "dialogState";
    String ATTR_DIRECTION = "direction";
    String ATTR_DISABLED = "disabled";
    String ATTR_DISPLAY_MODE = "displayMode";
    String ATTR_DISPLAY_REF = "displayRef";
    String ATTR_DISPLAY_TYPE = "displayType";
    String ATTR_DOCKABLE = "dockable";
    String ATTR_DRAG_SOURCE = "dragSource";
    String ATTR_ENUM_NAME = "enumName";
    String ATTR_ENUM_VALUE = "enumValue";
    String ATTR_EVENT_TYPE = "eventType";
    String ATTR_EXPAND = "expand";
    String ATTR_EXPANDED = "expanded";
    String ATTR_EXPRESSION = "expression";
    String ATTR_EXTENDS = "extends";
    String ATTR_FACTORY = "factory";
    String ATTR_FIELD = "field";
    String ATTR_FILTER = "filter";
    String ATTR_FILTERABLE = "filterable";
    String ATTR_FILTER_CRITERIA = "filterCriteria";
    String ATTR_FILTER_OPERATOR = "filterOperator";
    String ATTR_FIRST_INDEX = "firstIndex";
    String ATTR_FIXED = "fixed";
    String ATTR_FOCUS = "focus";
    String ATTR_FOLDER_NAME = "folderName";
    String ATTR_FORM_HANDLER = "formHandler";
    String ATTR_FORMAT = "format";
    String ATTR_GRID_DATA = "gridData";
    String ATTR_GROUP = "group";
    String ATTR_GROUP_SEPARATOR = "groupSeparator";
    String ATTR_HANDLE = "handle";
    String ATTR_HANDLER_RESULT = "handlerResult";
    String ATTR_HEIGHT = "height";
    String ATTR_HORIZONTAL_SCROLL = "horizontalScroll";
    String ATTR_ID = "id";
    String ATTR_INDEX = "index";
    String ATTR_IS_ACT_ON_BEHALF = "isActOnBehalf";
    String ATTR_IS_LOGGED_IN = "isLoggedIn";
    String ATTR_IS_QUERY = "isQuery";
    String ATTR_ITEM = "item";
    String ATTR_ITEMS = "items";
    String ATTR_KEY = "key";
    String ATTR_KEYS = "keys";
    String ATTR_LANGUAGE_CODE = "languageCode";
    String ATTR_LAYOUT = "layout";
    String ATTR_LAZY = "lazy";
    String ATTR_LINK = "link";
    String ATTR_LOG_EXCEPTION = "logException";
    String ATTR_LOG_LEVEL = "logLevel";
    String ATTR_LOG_MESSAGE = "logMessage";
    String ATTR_LOG_STACKTRACE = "logStackTrace";
    String ATTR_MAX_INACTIVE_INTERVAL = "maxInactiveInterval";
    String ATTR_MAX_WIDTH = "maxWidth";
    String ATTR_MAXIMIZE = "maximize";
    String ATTR_MEMBER = "member";
    String ATTR_MEMBER_FILTER = "memberFilter";
    String ATTR_MEMBER_ID = "memberId";
    String ATTR_MENU = "menu";
    String ATTR_MENU_ID = "menuId";
    String ATTR_MESSAGE = "message";
    String ATTR_MESSAGE_TYPE = "messageType";
    String ATTR_METADATA = "metadata";
    String ATTR_METHOD = "method";
    String ATTR_MINIMIZE = "minimize";
    String ATTR_MODAL = "modal";
    String ATTR_MODEL = "model";
    String ATTR_MODIFY = "modify";
    String ATTR_MODULE = "module";
    String ATTR_MOVE = "move";
    String ATTR_MULTI_SELECT = "multiSelect";
    String ATTR_MVC_INSTANCE_ID = "mvcInstanceId";
    String ATTR_NAME = "name";
    String ATTR_NEW_MESSAGES = "newMessages";
    String ATTR_OBJECT_NAME = "objectName";
    String ATTR_ON_ERROR = "onError";
    String ATTR_ON_SUCCESS = "onSuccess";
    String ATTR_ORDER = "order";
    String ATTR_PARAMETERS = "parameters";
    String ATTR_PARENT_CONTEXT_OBJECT = "parentContextObject";
    String ATTR_PASSWORD = "password";
    String ATTR_PATTERNS = "patterns";
    String ATTR_PATH = "path";
    String ATTR_PERSPECTIVE = "perspective";
    String ATTR_PINNABLE = "pinnable";
    String ATTR_POINT_INDEX = "pointIndex";
    String ATTR_POLL_INTERVAL = "pollInterval";
    String ATTR_POSITION = "position";
    String ATTR_PREFERENCE = "preference";
    @Deprecated String ATTR_PREVENTDRAG = "preventdrag";
    String ATTR_PRICE = "price";
    String ATTR_PROCESSOR = "processor";
    String ATTR_QUERY = "query";
    String ATTR_READ_MESSAGES = "readMessages";
    String ATTR_READ_ONLY = "readOnly";
    String ATTR_REF = "ref";
    String ATTR_REMOVE = "remove";
    String ATTR_REQUIRED = "required";
    String ATTR_RESIZABLE = "resizable";
    String ATTR_RESOURCE = "resource";
    String ATTR_RETURN_TYPE = "returnType";
    String ATTR_REQUEST = "request";
    String ATTR_ROUNDTRIP_TIME = "roundtripTime";
    String ATTR_ROW_COUNT = "rowCount";
    String ATTR_ROW_INDEX = "rowIndex";
    String ATTR_ROWS = "rows";
    String ATTR_SCROLL_DATA = "scrollData";
    String ATTR_SCREEN_ORIENTATION = "screenOrientation";
    String ATTR_SEGMENT_NO = "segmentNo";
    String ATTR_SEGMENT_COUNT = "segmentCount";
    String ATTR_SELECTED_COUNT = "selectedCount";
    String ATTR_SELECTED_INDEX = "selectedIndex";
    String ATTR_SELECTED_KEYS = "selectedKeys";
    String ATTR_SELECTED_OBJECT = "selectedObject";
    String ATTR_SERIES = "series";
    String ATTR_SERVER_NAME = "serverName";
    String ATTR_SHIFT_KEY_DOWN = "shiftKeyDown";
    String ATTR_SINGLETON = "singleton";
    String ATTR_SIZE = "size";
    String ATTR_SLOT = "slot";
    String ATTR_SNAP_TO_BOTTOM = "snapToBottom";
    String ATTR_SORT = "sort";
    String ATTR_SORT_COLUMN = "sortColumn";
    String ATTR_SORT_CRITERIA = "sortCriteria";
    String ATTR_SORT_ORDER = "sortOrder";
    String ATTR_SORT_PRIORITY = "sortPriority";
    String ATTR_SORTABLE = "sortable";
    String ATTR_SOURCE = "source";
    String ATTR_STATE = "state";
    String ATTR_STATUS_CODE = "status.code";
    String ATTR_STATUS_MESSAGE = "status.message";
    String ATTR_STATUS_SUBCODE = "status.subCode";
    String ATTR_STATUS_SUBMESSAGE = "status.subMessage";
    String ATTR_STATUS_TAPSTATUSMESSAGE = "status.tapStatusMessage";
    String ATTR_STYLE = "style";
    String ATTR_SUMMARIES = "summaries";
    String ATTR_SUMMARY = "summary";
    String ATTR_SYSTEM_NAME = "systemName";
    String ATTR_SYSTEM_VERSION = "systemVersion";
    String ATTR_TAG_NAME = "tagName";
    String ATTR_TAPSTATUSMESSAGE = "tapStatusMessage";
    String ATTR_TARGET = "target";
    String ATTR_TARGET_MVC_INSTANCE_ID = "targetMvcInstanceId";
    String ATTR_TARGET_VIEW = "targetView";
    String ATTR_TEMPLATE = "template";
    String ATTR_TEXT = "text";
    String ATTR_TITLE = "title";
    String ATTR_TOOLTIP = "tooltip";
    String ATTR_TOTAL_SIZE = "totalSize";
    String ATTR_TYPE = "type";
    String ATTR_UPDATE_CONTEXT = "updateContext";
    String ATTR_UPLOAD_HANDLER = "upload-handler";
    String ATTR_URL = "url";
    String ATTR_USE_FORM_HANDLER = "useFormHandler";
    String ATTR_USER = "user";
    String ATTR_USER_FILTER = "userFilter";
    String ATTR_USER_ID = "userId";
    String ATTR_VALUE = "value";
    String ATTR_VALUES = "values";
    String ATTR_VERSION = "version";
    String ATTR_VIEW = "view";
    String ATTR_VIEW_ID = "viewId";
    String ATTR_VIEWREF = "viewref";
    String ATTR_VISIBLE = "visible";
    String ATTR_VISIBLE_SIZE = "visibleSize";
    String ATTR_WIDTH = "width";
    String ATTR_XML_TEXT = "xml-text";
    
    // States and other reserved attributes
    String ATTR_CONTEXT_REMOVE = "_contextremove";
    String ATTR_CONTEXT_SOURCE = "_contextsource";
    String ATTR_CURRENT_VIEWPORT_EDITOR_LOCATION = "_currentViewportEditorLocation";
    String ATTR_DIRTY = "_dirty";
    String ATTR_FORM_STATE = "_formstate";
    String ATTR_MODEL_LABEL = "_modellabel";
    String ATTR_MODEL_NAME = "_modelname";
    String ATTR_MODEL_STATE = "_modelstate";
    String ATTR_NEW_OBJECT = "_newObject";
    String ATTR_OBJECT_KEY = "_objectKey";
    String ATTR_REQUEST_TOKEN = "_requesttoken";
    String ATTR_VIEW_TITLE = "_viewtitle";
    
    // tag names in xml models
    String TAG_AS_CACHE = "AsCache";
    String TAG_AS_COMPONENTS = "AsComponents";
    String TAG_AS_DATASOURCES = "AsDataSources";
    String TAG_AS_DEFAULTPATTERNS = "AsDefaultPatterns";
    String TAG_AS_DICTIONARY = "AsDictionary";    
    String TAG_AS_GET_METHODS = "AsGetMethods";
    String TAG_AS_GET_METHOD = "AsGetMethod";
    String TAG_AS_GET_METHOD_SOURCE = "AsGetMethodSource";
    String TAG_AS_LIST = "AsList";
    String TAG_AS_LIST_TREE = "AsListTree";
    String TAG_AS_LOCALE = "AsLocale";
    String TAG_AS_MENUS = "AsMenus";
    String TAG_AS_MVC = "AsMvc";
    String TAG_AS_PARAMETERS = "AsParameters";
    String TAG_AS_PERSPECTIVES = "AsPerspectives";
    String TAG_AS_TREE = "AsTree";
    String TAG_BLOB = "blob";
    String TAG_BUTTON = "button";
    String TAG_BUTTONPANEL = "buttonpanel";
    String TAG_CACHE_REFERENCE = "cache-reference";
    String TAG_CHART = "chart";
    String TAG_CHART_DATA = "chart-data";
    String TAG_CHART_FIELD = "chart-field";
    String TAG_CHART_OPTIONS = "chart-options";
    String TAG_CLEAR = "clear";
    String TAG_CLICK_EVENT = "click-event";
    String TAG_COMPONENT = "component";
    String TAG_CONFIGURATION = "Configuration";
    String TAG_CONTENT = "content";
    String TAG_CONTEXT = "context";
    String TAG_CONTEXT_LOOKUP = "context-lookup";
    String TAG_CONTEXT_MENU = "contextmenu";
    String TAG_CONTEXT_MENUS = "contextmenus";
    String TAG_CWF_PLUGIN = "CwfPlugin";
    String TAG_DELETE = "delete";
    String TAG_DISPLAY = "display";
    String TAG_FIELD = "field";
    String TAG_FIELDSET = "fieldset";
    String TAG_FOLDER = "folder";
    String TAG_FOOTER = "footer";
    String TAG_GET = "get";
    String TAG_GROUP = "group";
    String TAG_HEADER = "header";
    String TAG_LAYOUT = "layout";
    String TAG_LINK = "link";
    String TAG_MENU = "menu";
    String TAG_MENUITEM = "menuitem";
    String TAG_MENU_ITEMS = "menuitems";
    String TAG_NESTED_VIEWS = "nested-views";
    String TAG_NODE = "node";
    String TAG_PARAM = "param";
    String TAG_PARAMETER = "parameter";
    String TAG_PATTERN = "pattern";
    String TAG_PERSPECTIVE = "perspective";
    String TAG_POST = "post";
    String TAG_PUT = "put";
    String TAG_RESOURCE = "resource";
    String TAG_REST = "rest";
    String TAG_ROLE = "role";
    String TAG_ROOT = "root";
    String TAG_SET = "set";
    String TAG_SLOT = "slot";
    String TAG_SLOT_TEMPLATE = "slot-template";
    String TAG_SORT = "sort";
    String TAG_TARGET = "target";
    String TAG_VIEW = "view";
    
}
