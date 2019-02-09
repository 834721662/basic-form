package com.zj.basicform.common.server;

/**
 *
 * 获取Jar的MANIFEST.MF里的属性值,支持标准的Implementation-Version: ${pom.version} 获取。
 * 如果是Maven构建的项目,增加jar plugin的如下配置:
 *
 <plugin>
    <groupId>org.apache.maven.plugins</groupId>
         <artifactId>maven-jar-plugin</artifactId>
         <configuration>
            <archive>
                <manifest>
                    <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                </manifest>
            </archive>
         </configuration>
 </plugin>
 * @author zj
 * @since 2019/2/9
 */
public class ManifestVersionReader implements VersionReader {

    private Class c;

    public ManifestVersionReader(Class c) {
        this.c = c;
    }

    @Override
    public String version() {
        return c.getPackage().getImplementationVersion();
    }

}
