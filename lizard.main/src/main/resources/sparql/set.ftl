INSERT DATA 
{
<#if dataset??>
GRAPH <${dataset}>{
</#if>
<${uri}> <${property}> <${object}>
<#if dataset??>
}
</#if>
}