<html>
<head>
<script src="//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js"></script>
<style type="text/css">
table{
  border-collapse: collapse;
}

table, td, th {
	border: 1px solid #A24BAD;
	font-family: 'Oxygen', sans-serif;
}

td {
	text-align: center;
	vertical-align: middle;
}

th {
	background-color: #A24BAD;
	color: white;
}

body {
	text-align: center;
}

.container {
	margin-left: auto;
	margin-right: auto;
	width: 40em;
}

.action-panel {
	margin-bottom: 10px;
}

img {
	max-width: 500px;
}

.hide {
	display: none;
}
</style>
<script type="text/javascript">
	$(document).ready(function() {
		setInterval(showTableData, 100);
		$("#tablediv").hide();
		$("#showTable").click(showTableData);
		
		function showTableData(){
			$.ajax({
				  type: "POST",
				  url: "displayImage",
				  data: {
					    event : 'display'
				  },
				  success: function(responseJson){
					  if (responseJson != null && responseJson != "") {
							var row = "<tr><td>";
							row += responseJson.timestamp + "<br />(" + getDateTime(responseJson.timestamp) + ")</td>";
							row += "<td>" + responseJson.frameID + "</td>";
							
							row += "<td>";
							for (var i = 0; i < responseJson.images.length; i++) {
								row += "<img src='data:image/jpg;base64," + responseJson.images[i] + "' />";
							}

							row += "</td>";
							row += "<td><b>" + responseJson.objectCount + "</b></td>";
							row += "<td>" + responseJson.cameraID + "</td>";
							row += "<td>" + responseJson.cascade + "</td>";
							$('#receiveDataTable tr:first').after(row);
							
							$("#tablediv").show();
						}
				  },
				  error: function(xmlHttpRequest, textStatus, errorThrown) {
				     console.log(xmlHttpRequest.responseText);
				  }
			});
		}
		
		$("#clearQueueTable").click(function(){
			$.ajax({
				  type: "POST",
				  url: "displayImage",
				  data: {
					    event : 'clear'
				  },
				  success: function(responseJson){
					  
				  },
				  error: function(xmlHttpRequest, textStatus, errorThrown) {
					  console.log(xmlHttpRequest.responseText);
				  }
			});
			
			$("#receiveDataTable").find("tr:gt(0)").remove();
			$("#tablediv").hide();
		});
	});
	
	function getDateTime(timestamp){
		var date = new Date(timestamp);
		return date.toString();
	}
</script>
</head>
<body>
	<h1>Object Detection Results</h1>
	<div class="action-panel">
		<input type="button" value="Show Data" id="showTable" class="hide"/>
		<input type="button" value="Clear Queue" id="clearQueueTable" />
	</div>
	<div id="tablediv">
		<table id="receiveDataTable">
			<tr>
				<th scope="col">Timestamp</th>
				<th scope="col">Frame ID</th>
				<th scope="col">Image</th>
				<th scope="col">Detected Object Count</th>
				<th scope="col">Camera ID (Source)</th>
				<th scope="col">Cascade</th>
			</tr>
		</table>
	</div>
</body>
</html>
