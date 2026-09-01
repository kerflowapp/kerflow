# DATABASE

### Functions to create

```sql
CREATE OR REPLACE FUNCTION array_overlap(text[], text[]) RETURNS boolean AS
$$
SELECT $1 && $2;
$$ LANGUAGE SQL IMMUTABLE;
```