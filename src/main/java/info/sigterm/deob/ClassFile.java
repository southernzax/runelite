package info.sigterm.deob;

import info.sigterm.deob.attributes.Attributes;
import info.sigterm.deob.pool.Class;
import info.sigterm.deob.pool.NameAndType;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ClassFile
{
	private ClassGroup group;
	private DataInputStream is;

	private ClassFile parent; // super class
	private ArrayList<ClassFile> children = new ArrayList<ClassFile>(); // classes which inherit from this

	private int magic;
	private short minor_version;
	private short major_version;
	private ConstantPool pool;
	private short access_flags;
	private int this_class;
	private int super_class;
	private Interfaces interfaces;
	private Fields fields;
	private Methods methods;
	private Attributes attributes;

	public ClassFile(ClassGroup group, DataInputStream is) throws IOException
	{
		this.group = group;
		this.is = is;

		magic = is.readInt();
		if (magic != 0xcafebabe)
			throw new IOException("File is not a java class file.");

		minor_version = is.readShort();
		major_version = is.readShort();

		pool = new ConstantPool(this);

		access_flags = is.readShort();
		this_class = is.readUnsignedShort();
		super_class = is.readUnsignedShort();

		interfaces = new Interfaces(this);

		fields = new Fields(this);

		methods = new Methods(this);

		attributes = new Attributes(this);
	}

	public ClassGroup getGroup()
	{
		return group;
	}

	public DataInputStream getStream()
	{
		return is;
	}

	public ConstantPool getPool()
	{
		return pool;
	}

	public String getName()
	{
		Class entry = (Class) pool.getEntry(this_class);
		return entry.getName();
	}

	public ClassFile getParent()
	{
		Class entry = (Class) pool.getEntry(super_class);
		String superName = entry.getName();
		ClassFile other = group.findClass(superName);
		assert other != this;
		return other;
	}

	public Field findField(NameAndType nat)
	{
		Field f = fields.findField(nat);
		if (f != null)
			return f;

		ClassFile parent = getParent();
		if (parent != null)
			return parent.findField(nat);

		return null;
	}

	public void buildClassGraph()
	{
		ClassFile other = getParent();
		if (other == null)
			return; // inherits from a class not in my group

		this.parent = other;
		parent.children.add(this);
	}

	public void buildInstructionGraph()
	{
		methods.buildInstructionGraph();
	}
}
