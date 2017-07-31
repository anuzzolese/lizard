INSERT DATA 
{
<#if dataset??>
GRAPH <${dataset}>{
</#if>
<#list objects as object>
<${uri}> <${property}> <${object.individual.URI}>
</#list>
<#if dataset??>
}
</#if>
}