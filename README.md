# Evan Ritchey Project 1 ORM
## Light Weight ORM (LWORM)
___
### Description:
A super lightweight ORM whose design aims to use as few annotations as possible
### Instructions:
In any given class that you want to create a database table for, you must mark the fields you want tracked with:
> @SaveFieldLworm

That class MUST CONTAIN a zero arguments constructor
___
### Local install with Maven:

#### In the POM.xml
>       <dependencies>
>         <dependency>
>          <groupId>com.revature</groupId>
>          <artifactId>lworm</artifactId>
>          <version>1.2-SNAPSHOT</version>
>         <dependency>
>       <dependencies>



After annotaing the fields you want persisted we can then write:
> import persistence.orm.Lworm;
> 
> private Lworm lworm = new Lworm();

#### lworm now has access to:
> .create(obj)
> 
> .get(obj,fvp)
> 
> .update(obj,fvpValues,fvpConstraints) //fvpValues: the new values; fvpConstraints: constraints on the query
> 
> .delete(obj,fvp)

where "obj" is the object corresponding to the class with the annotated fields

#### FieldValuePair (fvp):

> Lworm.FieldValuePair[] fvp = new Lworm.FieldValuePair[#]
  
Which contains items of:
  
> Lworm.FieldValuePair("fieldName",value) //where value corresponds to the type of "field"

Each item of the list is a constraint on the database query

___

This project was created entirely in Java, relying heavily on reflections
