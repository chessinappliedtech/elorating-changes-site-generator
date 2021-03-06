<html lang="en">
<head>
    <title>${getTitle()}</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="stylesheet"
          href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css"
          integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO"
          crossorigin="anonymous">
    <style>
        .fixed-square {
            min-width: 50px;
            height: 50px;
        }
    </style>
</head>
<body>
<div class="container">
    <table class="table table-bordered table-hover">
        <caption style="text-align:left;caption-side: top">
            <h2>${getTitle()}</h2>
        </caption>
        <thead class="thead-light">
        <tr>
            <#list getHeaderRowView().getCells() as headerCell>
                <th class="text-center fixed-square">${headerCell.getValue()}</th>
            </#list>
        </tr>
        </thead>
        <tbody>
        <#list getPlayerRowViews() as row>
            <tr>
                <#list row.getCells() as cell>
                    <td class="text-center fixed-square" colspan="${cell.getColspan()}" rowspan="${cell.getRowspan()}">
                        <#if cell.getLink()??>
                            <a href="${cell.getLink()}">${cell.getValue()}</a>
                        <#else>
                            ${cell.getValue()}
                        </#if>
                    </td>
                </#list>
            </tr>
        </#list>
        </tbody>
    </table>
</div>
</body>
</html>
