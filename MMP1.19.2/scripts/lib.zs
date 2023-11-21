#priority 100
public expand string {
    public const join(names as string[], separator as char) as string => {

        var returned = "";
        for i, name in names {
            returned += name;
            if i != names.length-1 {
                returned += separator;
                returned += this;
            }
        }
        return returned;
    }
}