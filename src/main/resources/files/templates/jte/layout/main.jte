<%-- REPLACE WITH IMPORTS --%>
@import gg.jte.Content;
@param Content content
@param String title = ""
@param Content scriptContent = @``

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title}</title>
    <!-- REPLACE WITH HEAD TAGS -->
    <!-- REPLACE WITH EXTERNAL CSS LINKS -->
</head>
<body>
<main>
    ${content}
</main>
<!-- REPLACE WITH EXTERNAL JS LINKS -->
${scriptContent}
</body>
</html>
