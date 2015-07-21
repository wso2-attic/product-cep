<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<link href="../tenant-dashboard/css/dashboard-common.css" rel="stylesheet" type="text/css" media="all"/>
<%
        Object param = session.getAttribute("authenticated");
        String passwordExpires = (String) session.getAttribute(ServerConstants.PASSWORD_EXPIRATION);
    boolean hasInputEventAdaptorMgtPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/event-adaptors");
    boolean hasEventFormatterMgtPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/event-streams");
    boolean hasOutputEventAdaptorMgtPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/event-adaptors");
    boolean hasEventBuilderPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/event-streams");
    boolean hasExecutionPlanMgtPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/manage/event-processor");
    boolean hasStatsPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/event-streams");
    boolean hasStreamPermission = CarbonUIUtil.isUserAuthorized(request, "/permission/admin/monitor/event-streams");

    boolean loggedIn = false;
        if (param != null) {
            loggedIn = (Boolean) param;
        }

%>

<div id="passwordExpire">
         <%
         if (loggedIn && passwordExpires != null) {
         %>
              <div class="info-box"><p>Your password expires at <%=passwordExpires%>. Please change by visiting <a href="../user/change-passwd.jsp?isUserChange=true&returnPath=../admin/index.jsp">here</a></p></div>
         <%
             }
         %>
</div>
<div id="middle">
<div id="workArea">

    <style type="text/css">
        .tip-table td.cep1 {
            background-image: url(../../carbon/tenant-dashboard/images/cep-analtics-dashboard.png);
        }
        .tip-table td.cep2 {
            background-image: url(../../carbon/tenant-dashboard/images/cep-execution-mgr.png);
        }
        .tip-table td.cep3 {
            background-image: url(../../carbon/tenant-dashboard/images/cep-event-flow.png);
        }
        .tip-table td.cep4 {
            background-image: url(../../carbon/tenant-dashboard/images/cep-event-streams.png);
        }



        .tip-table td.cep5 {
            background-image: url(../../carbon/tenant-dashboard/images/cep-event-receivers.png);
        }
        .tip-table td.cep6 {
            background-image: url(../../carbon/tenant-dashboard/images/cep-event-publisher.png);
        }
        .tip-table td.cep7 {
            background-image: url(../../carbon/tenant-dashboard/images/cep-execution-plans.png);
        }
        .tip-table td.cep8 {
            background-image: url(../../carbon/tenant-dashboard/images/cep-monitoring-cep.png);
        }
    </style>
    <%--
    Capp, Bucket, Broker, Topic
   Services.  Monitor CEP, MonotorBucket, Mon-Broker,


    --%>
    <h2 class="dashboard-title">WSO2 CEP Quick Start Dashboard</h2>
    <table class="tip-table">
        <tr>
            <td class="tip-top cep1"></td>
            <td class="tip-empty"></td>
            <td class="tip-top cep2"></td>
            <td class="tip-empty "></td>
            <td class="tip-top cep3"></td>
            <td class="tip-empty "></td>
            <td class="tip-top cep4"></td>
        </tr>
        <tr>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                        if (hasInputEventAdaptorMgtPermission) {
                    %>
                    <a class="tip-title" href="../../../../portal/?region=region1&item=dashboard_menu_1">Dashboard</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Dashboard</h3> <br/>
                    <%
                        }
                    %>

                    <p>Visualizes event streams in real time.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">

                    <%
                        if (hasEventBuilderPermission) {
                    %>
                    <a class="tip-title" href="../execution-manager/domains_ajaxprocessor.jsp">Execution Manager</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Execution Manager</h3> <br/>
                    <%
                        }
                    %>

                    <p>User friendly dashboard to customize preconfigured domain-specific execution parameters.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                        if (hasExecutionPlanMgtPermission) {
                    %>
                    <a class="tip-title" href="../event-flow/index.jsp?region=region1&item=event_flow_menu">Event Flow</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Event Flow</h3> <br/>
                    <%
                        }
                    %>


                    <p>Visualizes event flow through all CEP operations.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                        if (hasEventFormatterMgtPermission) {
                    %>
                    <a class="tip-title" href="../eventstream/index.jsp?region=region1&item=eventstream_menu">Event Streams</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Event Streams</h3> <br/>
                    <%
                        }
                    %>

                     <p>Manages all streams defined in the system.</p>

                </div>
            </td>
        </tr>
        <tr>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
        </tr>
    </table>
    <div class="tip-table-div"></div>
    <table class="tip-table">
        <tr>
            <td class="tip-top cep5"></td>
            <td class="tip-empty"></td>
            <td class="tip-top cep6"></td>
            <td class="tip-empty"></td>
            <td class="tip-top cep7"></td>
            <td class="tip-empty"></td>
            <td class="tip-top cep8"></td>
        </tr>
        <tr>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                    if (hasOutputEventAdaptorMgtPermission) {
                    %>
                    <a class="tip-title" href="../eventreceiver/index.jsp?region=region1&item=eventreceiver_menu">Event Receivers</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Event Receivers</h3> <br/>
                    <%
                        }
                    %>


                    <p>Receives events from external event sources.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                        if (hasStatsPermission) {
                    %>
                    <a class="tip-title" href="../eventpublisher/index.jsp?region=region1&item=eventpublisher_menu">Event Publishers</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Event Publishers</h3> <br/>
                    <%
                        }
                    %>
                    <p>Publishes events to external event sinks.</p>
                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                        if (hasStreamPermission) {
                    %>
                    <a class="tip-title" href="../eventprocessor/index.jsp?region=region1&item=execution_plan_menu">Execution Plans</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Execution Plans</h3> <br/>
                    <%
                        }
                    %>
                    <p>Contains the execution logic that needs to be performed on the event streams.</p>

                </div>
            </td>
            <td class="tip-empty"></td>
            <td class="tip-content">
                <div class="tip-content-lifter">
                    <%
                        if (hasExecutionPlanMgtPermission) {
                    %>
                    <a class="tip-title" href="../event-statistics/event_statistics_view.jsp?region=region4&item=event_statistics_menu">Monitoring CEP</a> <br/>
                    <%
                    } else {
                    %>
                    <h3 class="tip-title">Monitoring CEP</h3> <br/>
                    <%
                        }
                    %>
                    <p>Shows real-time accumulative statistics of all CEP operations.</p>

                </div>
            </td>
            <%--<td class="tip-empty"></td>--%>
            <%--<td class="tip-content-empty"></td>--%>
            <%--<td class="tip-empty"></td>--%>
            <%--<td class="tip-content-empty"></td>--%>
        </tr>
        <tr>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
            <td class="tip-empty"></td>
            <td class="tip-bottom"></td>
        </tr>
    </table>
<p>
    <br/>
</p> </div>
</div>
