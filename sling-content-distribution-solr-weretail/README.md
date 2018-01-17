Solr schema:

```
<schema>
  <!-- standard field types as defined in the solr examples -->

  <!-- fields required by solr -->
  <field name="_root_" type="string" docValues="false" indexed="true" stored="false"/>
  <field name="_text_" type="text_general" multiValued="true" indexed="true" stored="false"/>
  <field name="_version_" type="plong" indexed="false" stored="false"/>
  <field name="id" type="string" multiValued="false" indexed="true" required="true" stored="true"/>
  <!-- fields required by sling -->
  <field name="_path" type="descendent_path" multiValued="false" indexed="true" required="true" />
  <field name="_name" type="string" multiValued="false" indexed="true" required="true" />
  <field name="_indexedAt" type="pdate" multiValued="false" indexed="true" required="true" />
  <!-- we retail fields -->
  <field name="weretail_title" type="text_en" multiValued="false" indexed="true" />
  <field name="weretail_description" type="text_en" multiValued="false" indexed="true" />
  <field name="weretail_text" type="text_en" multiValued="true" indexed="true" />
  <field name="weretail_tags" type="text_en" multiValued="true" indexed="true" />
</schema>
```