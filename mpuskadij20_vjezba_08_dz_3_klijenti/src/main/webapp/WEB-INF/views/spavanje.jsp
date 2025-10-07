<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Spavanje tvrtke</title>
</head>
<body>
	   <h1>Spavanje tvrtke</h1>
	   
	    <ul>
            <li>
                <a href="${pageContext.servletContext.contextPath}/mvc/tvrtka/pocetak">Početna stranica</a>
		</li>      
        </ul>  
        
        <form action="${pageContext.servletContext.contextPath}/mvc/tvrtka/admin/spavanje" method="get">
        	<input type="number" min=1 name="vrijeme">
        	<button type="submit">Uspavaj tvrtku</button>
        </form>
	
    <p>${status}</p>
</body>
</html>
