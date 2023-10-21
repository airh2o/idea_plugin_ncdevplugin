# 元数据文件结构说明

## 标签 component 记录 bmf文件总体情况，md_component

## 标签 component > celllist 记录 bmf文件里画布里的对象，比如实体，引用的接口，枚举

 bpf文件的结构暂时不研究，不处理！ 标签 component > celllist > busioperation 记录 bmf文件里画布里的对象， 业务操作

## 标签 component > celllist > Reference 记录 bmf文件里画布里的对象，引用的接口或对象

1. id=唯一标记（随机生成） 也等于 connectlist > busiitfconnection 标签里的target ， 一个 Reference 要加一个 busiitfconnection
2. componentID=当前bmf文件md_component id
3. fullClassName=一般格式用 nc.vo.当前bmf名称空间.当前bmf名称.reference
4. mdFilePath=引用的对象的所在元数据文件 比如 \pf\pfbizitf.bmf
5. moduleName=引用的对象的所在元数据文件所在模块 比如 baseapp
6. name="reference"
7. refId=引用的对象的 md_class的id 也等于 connectlist > busiitfconnection 标签里的 realtarget
8. 注意 这个标签对应的 connectlist > busiitfconnection 标签的属性：
    1. componentID=componentID
    2. id=唯一标记 随机生成
    3. name="busiItf connection"
    4. realsource=实现引用的这个接口的entity的的id（通过 关联关系工具箱 > 实现）
    5. realtarget=refId
    6. source=realsource
    7. target=Reference标签的id属性

## 标签 component > celllist > entity 记录 bmf文件里画布里的对象，实体

## 标签 component > celllist > entity > attributelist 记录 实体的 属性列表 > attribute 属性

## 标签 component > celllist > entity > operationlist 记录 实体的 鬼知道是啥 好像没有元数据用过

## 标签 component > celllist > entity > busiitfs > itfid 记录 实体的 实现的所有接口的md_class  id 也就是Reference的refId

## 标签 component > celllist > entity > busimaps > busimap 记录 实体的 实现的所有接口的完整信息

1. attrid 是业务接口属性映射-映射属性 的(实体的)字段id
2. busiitfattrid 是引用的接口的属性(字段)id或实体的属性id （md_property的id）
3. cellid 是实体id
4. busiitfid 是引用的接口实体id

## 标签 component > celllist > entity > canzhaolist 记录 实体的 鬼知道是啥 好像没有元数据用过

## 标签 component > celllist > entity > accessor 记录 实体访问方式

## 标签 component > celllist > entity > accessor > properties 记录 实体的 鬼知道是啥 好像没有元数据用过

## 标签 component > celllist > Enumerate 记录 bmf文件里画布里的对象，枚举

## 标签 component > celllist > Enumerate > enumitemlist 记录 bmf文件里画布里的对象，枚举的值列表

## 标签 connectlist 记录

## 标签 connectlist > AggregationRelation 记录 关联关系工具箱

1. componentID 当前bmf文件组件id
2. id 标签唯一随机id
3. name="Aggregation Relation"
4. realsource = source   被实现者的实体id
5. realtarget = target   实现者的实体id
6. srcAttributeid  =  被实现者的实体里的属性id

## 标签 connectlist > busiitfconnection 记录 关联关系工具箱关系，和 celllist > Reference 标签 一对一！

1. bizItfImpClassName 关联线条里的 业务接口实现类
2. componentID 当前bmf文件组件id
3. id 标签唯一随机id
4. name="busiItf connection"
5. realsource = source 当前bmf里的实体id
6. realtarget 被实现的接口的实体id
7. target 对应的 celllist > Reference 标签 的id

## 标签 refdepends 记录 引用其他元数据文件里的md_class的id列表

## 标签 refdepends > dependfile 记录 引用其他元数据文件里的md_class的id

和 component > celllist > Reference 标签 一对一关系！

## 标签 dependfile 记录 依赖的元数据文件

1.entityid=md_class的id

## 标签 refdependLoseIDs 记录 鬼知道是啥 好像没有元数据用过

## 标签 cellRemoveLog 记录 鬼知道是啥 好像没有元数据用过

## 标签 rulers 记录 鬼知道是啥 好像没有元数据用过

## 标签 rulers > ruler 记录 鬼知道是啥 好像没有元数据用过

## 标签 rulers > ruler > guideList 记录 鬼知道是啥 好像没有元数据用过

## 主子表典型的bmf文件：

````xml


<?xml version="1.0" encoding="UTF-8"?>
<component conRouterType="曼哈顿直角" createIndustry="0" createTime="2023-10-19 22:28:08" creator="yonyouBQ" description=""
           displayName="testmeta001的" fromSourceBmf="true" gencodestyle="NC传统样式" help=""
           id="b102855a-daa7-4af0-9936-f8e00e7fcb39" industry="0" industryChanged="false" industryIncrease="false"
           industryName="" isSource="false" isbizmodel="false" mainEntity="11c55b01-7535-47ea-904b-5a8ee0fbf397"
           metaType=".bmf" modifier="yonyouBQ" modifyIndustry="0" modifyTime="2023-10-20 14:42:31" name="testmeta001"
           namespace="slxm" ownModule="slxm" preLoad="false" programcode="0" resModuleName="testmeta001" resid=""
           version="15" versionType="0">
    <celllist>
        <Reference componentID="b102855a-daa7-4af0-9936-f8e00e7fcb39" createIndustry="0"
                   createTime="2023-10-19 22:30:48" creator="yonyouBQ" description="" displayName="引用：流程信息获取、回写"
                   fullClassName="nc.vo.slxm.test.reference0" height="100" help=""
                   id="057b4b17-84d7-4aba-86d8-c4a4aa8fdf2d" industryChanged="false" isSource="true"
                   mdFilePath="f\pfbizitf.bmf" modifier="yonyouBQ" modifyIndustry="0" modifyTime="2023-10-19 22:32:51"
                   moduleName="baseapp" name="reference0" refId="2fd714f0-b6b0-4aeb-906e-ead92767ce32" resid=""
                   versionType="0" visibility="public" width="100" x="33" y="26"/>
        <entity accessorClassName="nc.md.model.access.javamap.POJOStyle" bizItfImpClassName=""
                componentID="b102855a-daa7-4af0-9936-f8e00e7fcb39" createIndustry="0" createTime="2023-10-19 22:31:29"
                creator="yonyouBQ" czlist="" description="" displayName="testmeta001实体1"
                fullClassName="nc.vo.slxm.testmeta001.Testmeta001entity1" height="100" help=""
                id="11c55b01-7535-47ea-904b-5a8ee0fbf397" industryChanged="false" isAuthen="true" isCreateSQL="true"
                isExtendBean="false" isPrimary="true" isSource="true"
                keyAttributeId="2810f5e2-5205-4937-b258-ca7813d933f5" modInfoClassName="" modifier="yonyouBQ"
                modifyIndustry="0" modifyTime="2023-10-21 15:13:28" name="testmeta001entity1" resid="" stereoType=""
                tableName="slxm_testmeta001entity1" userDefClassName="" versionType="0" visibility="public" width="100"
                x="204" y="38">
            <attributelist>
                <attribute accessStrategy="" accesspower="false" accesspowergroup="" calculation="false"
                           classID="11c55b01-7535-47ea-904b-5a8ee0fbf397" createIndustry="0"
                           dataType="BS000010000100001051" dataTypeStyle="SINGLE" dbtype="char" defaultValue=""
                           description="" displayName="这是id" dynamic="false" dynamicTable="" fieldName="pk"
                           fieldType="char" fixedLength="false" forLocale="false" help=""
                           id="2810f5e2-5205-4937-b258-ca7813d933f5" industryChanged="false" isActive="true"
                           isAuthorization="true" isDefaultDimensionAttribute="false" isDefaultMeasureAttribute="false"
                           isFeature="false" isGlobalization="false" isHide="false" isKey="true" isNullable="false"
                           isReadOnly="false" isShare="false" isSource="true" length="50" maxValue="" minValue=""
                           modifyIndustry="0" name="pk" notSerialize="false" precise="" refModelName="" resid=""
                           sequence="0" typeDisplayName="UFID" typeName="UFID" versionType="0" visibility="public"/>
                <attribute accessStrategy="" accesspower="false" accesspowergroup="" calculation="false"
                           classID="11c55b01-7535-47ea-904b-5a8ee0fbf397" createIndustry="0"
                           dataType="3b6dd171-2900-47f3-bfbe-41e4483a2a65" dataTypeStyle="REF" dbtype=""
                           defaultValue="~" description="" displayName="集团id" dynamic="false" dynamicTable=""
                           fieldName="pk_gropp" fieldType="varchar" fixedLength="false" forLocale="false" help=""
                           id="177d8785-34a9-4c86-9818-b6325b300ebb" industryChanged="false" isActive="true"
                           isAuthorization="true" isDefaultDimensionAttribute="false" isDefaultMeasureAttribute="false"
                           isFeature="false" isGlobalization="false" isHide="false" isKey="false" isNullable="true"
                           isReadOnly="false" isShare="false" isSource="true" length="20" maxValue="" minValue=""
                           modifyIndustry="0" name="pk_gropp" notSerialize="false" precise="" refModelName="集团" resid=""
                           sequence="1" typeDisplayName="组织_集团" typeName="group" versionType="0" visibility="public"/>
                <attribute accessStrategy="" accesspower="false" accesspowergroup="" calculation="false"
                           classID="11c55b01-7535-47ea-904b-5a8ee0fbf397" createIndustry="0"
                           dataType="11c55b01-7535-47ea-904b-5a8ee0fbf397" dataTypeStyle="REF" dbtype=""
                           defaultValue="~" description="" displayName="表体列表" dynamic="false" dynamicTable=""
                           fieldName="pk" fieldType="varchar" fixedLength="false" forLocale="false" help=""
                           id="d812df7d-b523-4dc2-9108-710d02e3b448" industryChanged="false" isActive="true"
                           isAuthorization="true" isDefaultDimensionAttribute="false" isDefaultMeasureAttribute="false"
                           isFeature="false" isGlobalization="false" isHide="false" isKey="false" isNullable="true"
                           isReadOnly="false" isShare="false" isSource="true" length="20" maxValue="" minValue=""
                           modifyIndustry="0" name="bodylist" notSerialize="false" precise="" refModelName="" resid=""
                           sequence="2" typeDisplayName="testmeta001实体1" typeName="testmeta001entity1" versionType="0"
                           visibility="public"/>
                <attribute accessStrategy="" accesspower="false" accesspowergroup="" calculation="false"
                           classID="11c55b01-7535-47ea-904b-5a8ee0fbf397" createIndustry="0"
                           dataType="50816e09-6db0-4f86-8666-b66ad9312238" dataTypeStyle="SINGLE" dbtype=""
                           defaultValue="" description="" displayName="性别" dynamic="false" dynamicTable=""
                           fieldName="sex" fieldType="varchar" fixedLength="false" forLocale="false" help=""
                           id="60a3f913-eef4-4891-9ba3-b62c744d3e2d" industryChanged="false" isActive="true"
                           isAuthorization="true" isDefaultDimensionAttribute="false" isDefaultMeasureAttribute="false"
                           isFeature="false" isGlobalization="false" isHide="false" isKey="false" isNullable="true"
                           isReadOnly="false" isShare="false" isSource="true" length="50" maxValue="" minValue=""
                           modifyIndustry="0" name="sex" notSerialize="false" precise="" refModelName="" resid=""
                           sequence="3" typeDisplayName="性别枚举" typeName="testmeta001enum1" versionType="0"
                           visibility="public"/>
                <attribute accessStrategy="" accesspower="false" accesspowergroup="" calculation="false"
                           classID="11c55b01-7535-47ea-904b-5a8ee0fbf397" createIndustry="0"
                           dataType="BS000010000100001001" dataTypeStyle="SINGLE" dbtype="varchar" defaultValue=""
                           description="" displayName="attrDisplayName4" dynamic="false" dynamicTable=""
                           fieldName="attrname4" fieldType="varchar" fixedLength="false" forLocale="false" help=""
                           id="fe01d05a-d361-4e47-b598-fc7608dc63f7" industryChanged="false" isActive="true"
                           isAuthorization="true" isDefaultDimensionAttribute="false" isDefaultMeasureAttribute="false"
                           isFeature="false" isGlobalization="false" isHide="false" isKey="false" isNullable="true"
                           isReadOnly="false" isShare="false" isSource="true" length="50" maxValue="" minValue=""
                           modifyIndustry="0" name="attrname4" notSerialize="false" precise="" refModelName="" resid=""
                           sequence="4" typeDisplayName="String" typeName="String" versionType="0" visibility="public"/>
                <attribute accessStrategy="" accesspower="false" accesspowergroup="" calculation="false"
                           classID="11c55b01-7535-47ea-904b-5a8ee0fbf397" createIndustry="0"
                           dataType="BS000010000100001001" dataTypeStyle="SINGLE" dbtype="varchar" defaultValue=""
                           description="" displayName="attrDisplayName5" dynamic="false" dynamicTable=""
                           fieldName="attrname5" fieldType="varchar" fixedLength="false" forLocale="false" help=""
                           id="4e9be7e3-68bd-4d48-ba36-4f3d892c534d" industryChanged="false" isActive="true"
                           isAuthorization="true" isDefaultDimensionAttribute="false" isDefaultMeasureAttribute="false"
                           isFeature="false" isGlobalization="false" isHide="false" isKey="false" isNullable="true"
                           isReadOnly="false" isShare="false" isSource="true" length="50" maxValue="" minValue=""
                           modifyIndustry="0" name="attrname5" notSerialize="false" precise="" refModelName="" resid=""
                           sequence="5" typeDisplayName="String" typeName="String" versionType="0" visibility="public"/>
                <attribute accessStrategy="" accesspower="false" accesspowergroup="" calculation="false"
                           classID="11c55b01-7535-47ea-904b-5a8ee0fbf397" createIndustry="0"
                           dataType="dbd76fb6-83bd-4af5-9316-3ec6d2201466" dataTypeStyle="ARRAY" dbtype=""
                           defaultValue="" description="" displayName="id_testmeta001entity1body" dynamic="false"
                           dynamicTable="" fieldName="pk" fieldType="" fixedLength="false" forLocale="false" help=""
                           id="6a53e3ab-b266-432f-ab09-9c562f5ef5f9" industryChanged="false" isActive="true"
                           isAuthorization="true" isDefaultDimensionAttribute="false" isDefaultMeasureAttribute="false"
                           isFeature="false" isGlobalization="false" isHide="false" isKey="false" isNullable="true"
                           isReadOnly="false" isShare="false" isSource="true" length="" maxValue="" minValue=""
                           modifyIndustry="0" name="id_testmeta001entity1body" notSerialize="false" precise=""
                           refModelName="" resid="" sequence="6" typeDisplayName="testmeta001entity1表体"
                           typeName="testmeta001entity1body" versionType="0" visibility="public"/>
            </attributelist>
            <operationlist/>
            <busiitfs>
                <itfid>2fd714f0-b6b0-4aeb-906e-ead92767ce32</itfid>
            </busiitfs>
            <busimaps>
                <busimap attrid="2810f5e2-5205-4937-b258-ca7813d933f5" attrpath="" attrpathid=""
                         busiitfattrid="eb4f78a8-7a2d-4aa4-9817-f4418d11f25e"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="d0eab74e-d8cd-4bba-bfa1-4920b307b565"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="ef7713f8-4707-456a-92fe-091785b0c65e"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="aa80f66e-f1e3-47cd-818c-e3218b7e207e"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="4458032b-f71e-46f5-9826-ce458009d21d"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="826357b1-1210-4ecf-8ebc-4d2dc0ab2813"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="0d045cc9-c178-489d-abbf-17c997e3b790"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="bf83cd87-aafe-40b1-9545-ed506c8b7214"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="7d07c873-b21f-4d4c-a21c-92c43b9b7786"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="9d7601f0-4ed7-4dca-868d-1c2a976fe458"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="f82d84cd-d01d-4861-b4f4-4c708ded0c5b"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="06e835cd-ea1d-4fea-b592-b702e228ae57"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="af163711-e50e-4692-8245-7694137d8896"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="d812df7d-b523-4dc2-9108-710d02e3b448" attrpath="" attrpathid=""
                         busiitfattrid="0bcd812c-4a13-4e86-83a9-24dbbf6e9cc2"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="e47f4da8-b109-472e-b4d9-f70ede7ef35f"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
                <busimap attrid="" attrpath="" attrpathid="" busiitfattrid="e112362d-181f-48d1-9e04-a96c95ec894a"
                         busiitfid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                         cellid="11c55b01-7535-47ea-904b-5a8ee0fbf397"/>
            </busimaps>
            <canzhaolist/>
            <accessor classFullname="nc.md.model.access.javamap.POJOStyle" displayName="POJO" name="pojo">
                <properties/>
            </accessor>
        </entity>
        <entity accessorClassName="nc.md.model.access.javamap.POJOStyle" bizItfImpClassName=""
                componentID="b102855a-daa7-4af0-9936-f8e00e7fcb39" createIndustry="0" createTime="2023-10-20 14:00:16"
                creator="yonyouBQ" czlist="" description="" displayName="testmeta001entity1表体"
                fullClassName="nc.vo.slxm.testmeta001.Testmeta001entity1body" height="100" help=""
                id="dbd76fb6-83bd-4af5-9316-3ec6d2201466" industryChanged="false" isAuthen="true" isCreateSQL="true"
                isExtendBean="false" isPrimary="false" isSource="true"
                keyAttributeId="f36aec6d-2ec3-4f43-9caa-78ccc664a611" modInfoClassName="" modifier="yonyouBQ"
                modifyIndustry="0" modifyTime="2023-10-21 15:13:28" name="testmeta001entity1body" resid="" stereoType=""
                tableName="slxm_testmeta001entity1body" userDefClassName="" versionType="0" visibility="public"
                width="100" x="488" y="28">
            <attributelist>
                <attribute accessStrategy="" accesspower="false" accesspowergroup="" calculation="false"
                           classID="dbd76fb6-83bd-4af5-9316-3ec6d2201466" createIndustry="0"
                           dataType="BS000010000100001051" dataTypeStyle="SINGLE" dbtype="char" defaultValue=""
                           description="" displayName="表体id" dynamic="false" dynamicTable="" fieldName="pk_body"
                           fieldType="char" fixedLength="false" forLocale="false" help=""
                           id="f36aec6d-2ec3-4f43-9caa-78ccc664a611" industryChanged="false" isActive="true"
                           isAuthorization="true" isDefaultDimensionAttribute="false" isDefaultMeasureAttribute="false"
                           isFeature="false" isGlobalization="false" isHide="false" isKey="true" isNullable="false"
                           isReadOnly="false" isShare="false" isSource="true" length="50" maxValue="" minValue=""
                           modifyIndustry="0" name="pk_body" notSerialize="false" precise="" refModelName="" resid=""
                           sequence="0" typeDisplayName="UFID" typeName="UFID" versionType="0" visibility="public"/>
                <attribute accessStrategy="" accesspower="false" accesspowergroup="" calculation="false"
                           classID="dbd76fb6-83bd-4af5-9316-3ec6d2201466" createIndustry="0"
                           dataType="BS000010000100001001" dataTypeStyle="SINGLE" dbtype="varchar" defaultValue=""
                           description="" displayName="上游id" dynamic="false" dynamicTable="" fieldName="srcid"
                           fieldType="varchar" fixedLength="false" forLocale="false" help=""
                           id="068292ed-7b06-46a2-be14-1997991ae5d4" industryChanged="false" isActive="true"
                           isAuthorization="true" isDefaultDimensionAttribute="false" isDefaultMeasureAttribute="false"
                           isFeature="false" isGlobalization="false" isHide="false" isKey="false" isNullable="true"
                           isReadOnly="false" isShare="false" isSource="true" length="50" maxValue="" minValue=""
                           modifyIndustry="0" name="srcid" notSerialize="false" precise="" refModelName="" resid=""
                           sequence="1" typeDisplayName="String" typeName="String" versionType="0" visibility="public"/>
            </attributelist>
            <operationlist/>
            <busiitfs>
                <itfid>5205ef20-5eae-4c75-bad8-16639152e622</itfid>
            </busiitfs>
            <busimaps/>
            <canzhaolist/>
            <accessor classFullname="nc.md.model.access.javamap.POJOStyle" displayName="POJO" name="pojo">
                <properties/>
            </accessor>
        </entity>
        <Enumerate componentID="b102855a-daa7-4af0-9936-f8e00e7fcb39" createIndustry="0"
                   createTime="2023-10-20 14:26:38" creator="yonyouBQ" dataType="BS000010000100001001" dbtype="varchar"
                   description="" displayName="性别枚举" fullClassName="nc.vo.slxm.testmeta001.Enumerate0" height="100"
                   help="" id="50816e09-6db0-4f86-8666-b66ad9312238" industryChanged="false" isSource="true"
                   modInfoClassName="" modifier="yonyouBQ" modifyIndustry="0" modifyTime="2023-10-20 14:27:17"
                   name="testmeta001enum1" resid="" typeDisplayName="String" typeName="String" versionType="0"
                   visibility="public" width="100" x="666" y="65">
            <enumitemlist>
                <enumitem createIndustry="0" description="男人哇" enumDisplay="男的"
                          enumID="50816e09-6db0-4f86-8666-b66ad9312238" enumValue="1" hidden="false"
                          id="ffd5016f-7853-4f63-bd54-361abc785240" industryChanged="false" isSource="false"
                          modifyIndustry="0" resid="" sequence="0" versionType="0"/>
                <enumitem createIndustry="0" description="" enumDisplay="女的"
                          enumID="50816e09-6db0-4f86-8666-b66ad9312238" enumValue="2" hidden="false"
                          id="f90e8800-e9c2-4551-abd5-44582d70c368" industryChanged="false" isSource="false"
                          modifyIndustry="0" resid="" sequence="1" versionType="0"/>
            </enumitemlist>
        </Enumerate>
        <Reference componentID="b102855a-daa7-4af0-9936-f8e00e7fcb39" createIndustry="0"
                   createTime="2023-10-20 14:42:31" creator="yonyouBQ" description="" displayName="引用：单据主子VO查询"
                   fullClassName="nc.vo.slxm.testmeta001.reference1" height="100" help=""
                   id="529ef491-b3eb-4a3d-9704-a9db5a91ea0f" industryChanged="false" isSource="true"
                   mdFilePath="f\pfbizitf.bmf" modifier="yonyouBQ" modifyIndustry="0" modifyTime="2023-10-20 14:42:38"
                   moduleName="baseapp" name="reference1" refId="5205ef20-5eae-4c75-bad8-16639152e622" resid=""
                   versionType="0" visibility="public" width="100" x="341" y="137"/>
    </celllist>
    <connectlist>
        <busiitfconnection bizItfImpClassName="" componentID="b102855a-daa7-4af0-9936-f8e00e7fcb39" createIndustry="0"
                           createTime="2023-10-19 22:32:51" creator="" description="" displayName="" help=""
                           id="b7c05384-5503-46c3-b759-a7513675d03c" industryChanged="false" isSource="false"
                           modifier="yonyouBQ" modifyIndustry="0" modifyTime="2023-10-19 22:32:51"
                           name="busiItf connection" realsource="11c55b01-7535-47ea-904b-5a8ee0fbf397"
                           realtarget="2fd714f0-b6b0-4aeb-906e-ead92767ce32"
                           source="11c55b01-7535-47ea-904b-5a8ee0fbf397" target="057b4b17-84d7-4aba-86d8-c4a4aa8fdf2d"
                           versionType="0">
            <points/>
        </busiitfconnection>
        <AggregationRelation componentID="b102855a-daa7-4af0-9936-f8e00e7fcb39" createIndustry="0"
                             createTime="2023-10-20 14:04:08" creator="" description="" displayName="" help=""
                             id="8297a8ad-9d53-4d95-9e0c-b35983d019f7" industryChanged="false" isSource="false"
                             modifier="yonyouBQ" modifyIndustry="0" modifyTime="2023-10-20 14:04:08"
                             name="Aggregation Relation" realsource="11c55b01-7535-47ea-904b-5a8ee0fbf397"
                             realtarget="dbd76fb6-83bd-4af5-9316-3ec6d2201466"
                             source="11c55b01-7535-47ea-904b-5a8ee0fbf397" sourceConstraint="1"
                             srcAttributeid="6a53e3ab-b266-432f-ab09-9c562f5ef5f9"
                             target="dbd76fb6-83bd-4af5-9316-3ec6d2201466" targetConstraint="1..n" versionType="0">
            <points/>
        </AggregationRelation>
        <busiitfconnection bizItfImpClassName="com.air.aaaa.ttt001" componentID="b102855a-daa7-4af0-9936-f8e00e7fcb39"
                           createIndustry="0" createTime="2023-10-20 14:42:38" creator="" description="" displayName=""
                           help="" id="e7ae3b7d-f0eb-489e-99b4-aa8a1456d1e2" industryChanged="false" isSource="false"
                           modifier="yonyouBQ" modifyIndustry="0" modifyTime="2023-10-20 14:42:38"
                           name="busiItf connection" realsource="dbd76fb6-83bd-4af5-9316-3ec6d2201466"
                           realtarget="5205ef20-5eae-4c75-bad8-16639152e622"
                           source="dbd76fb6-83bd-4af5-9316-3ec6d2201466" target="529ef491-b3eb-4a3d-9704-a9db5a91ea0f"
                           versionType="0">
            <points/>
        </busiitfconnection>
    </connectlist>
    <refdepends>
        <dependfile entityid="2fd714f0-b6b0-4aeb-906e-ead92767ce32"/>
        <dependfile entityid="3b6dd171-2900-47f3-bfbe-41e4483a2a65"/>
    </refdepends>
    <refdependLoseIDs/>
    <cellRemoveLog/>
    <rulers>
        <ruler isHorizontal="true" unit="0">
            <guideList/>
        </ruler>
        <ruler isHorizontal="false" unit="0">
            <guideList/>
        </ruler>
    </rulers>
</component>


````