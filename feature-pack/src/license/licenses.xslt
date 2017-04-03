<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"/>
    <xsl:param name="version" /> 
    <xsl:param name="timestamp" /> 
    <xsl:template match="/">
        <html>
            <head><title>Lisences Summary</title></head>
            <body>
                <h1>Summary of licenses for EAP <xsl:value-of select="$version"/> produced at <xsl:value-of select="$timestamp"/>.</h1>
                The following material has been provided for informational purposes only, and should not be relied upon or construed as a legal opinion or legal advice.
                <br/><br/>
                <table border="1">
		      <tr bgcolor="#9acd32">
			<th>Package Group</th>
			<th>Package Artifact</th>
                        <th>Package Version</th>
                        <th>Remote Licenses</th>
		      </tr>
		      <xsl:for-each select="licenseSummary/dependencies/dependency">
		      <tr>
			<td><xsl:value-of select="groupId"/></td>
			<td><xsl:value-of select="artifactId"/></td>
                        <td><xsl:value-of select="licenses/license/name"/></td>
                        <td><xsl:element name="a">
			    <xsl:attribute name="href">
				<xsl:value-of select="licenses/license/url"/>
			    </xsl:attribute>
			    <xsl:value-of select="licenses/license/url"/>
			</xsl:element></td>
		      </tr>
		      </xsl:for-each>
		</table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
